slint::include_modules!();

use std::net::{IpAddr, Ipv4Addr, UdpSocket};
use std::thread;
use std::time::{Duration, Instant};

const UDP_PORT: u16 = 9999;
const PACKET_LEN: usize = 41;
const DISCONNECT_TIMEOUT: Duration = Duration::from_millis(1500);

/// A telefon által küldött felfedező üzenet (broadcast). Ha ezt látjuk,
/// tudja a telefon, hogy itt a PC szerver, és visszaküldjük az IP-címünket.
const DISCOVERY_MSG: &[u8] = b"GYRECTION_DISCOVERY";
const DISCOVERY_RESPONSE_PREFIX: &str = "GYRECTION_IP ";

/// Megkeresi a gép helyi (LAN) IP-címét egy küldés nélküli UDP "probe"-bal,
/// hogy a felület alján megmutathassuk, hova kell a telefonban küldeni.
fn local_ipv4() -> IpAddr {
    if let Ok(probe) = UdpSocket::bind((Ipv4Addr::UNSPECIFIED, 0)) {
        if probe.connect((Ipv4Addr::new(8, 8, 8, 8), 80)).is_ok() {
            if let Ok(addr) = probe.local_addr() {
                return addr.ip();
            }
        }
    }
    IpAddr::V4(Ipv4Addr::UNSPECIFIED)
}

fn main() -> Result<(), slint::PlatformError> {
    let app = AppWindow::new()?;
    let app_weak = app.as_weak();

    // A PC helyi címe, amelyet a telefonban meg kell adni
    let ip = local_ipv4();
    let ip_string = ip.to_string();
    let server_info = format!("WiFi UDP | telefon erre küldjön: {ip}:{UDP_PORT}");
    app.set_server_status_text(server_info.clone().into());
    println!("{server_info}");

    // UDP szerver szál: folyamatosan fogadja a telefon datagramjait
    thread::spawn(move || {
        let socket = UdpSocket::bind((Ipv4Addr::UNSPECIFIED, UDP_PORT))
            .unwrap_or_else(|e| panic!("Nem sikerült UDP szervert indítani a {UDP_PORT}. porton: {e}"));
        socket
            .set_read_timeout(Some(Duration::from_millis(200)))
            .expect("Nem sikerült beállítani a read timeoutot.");
        println!("UDP szerver figyel a 0.0.0.0:{UDP_PORT} porton (WiFi).");

        let mut last_packet = Instant::now();
        let mut is_live = false;

        let mut buf = [0u8; 64];
        loop {
            match socket.recv_from(&mut buf) {
                Ok((size, src)) => {
                    // Broadcast-felfedezés: a telefon keresi a PC-t → küldjük az IP-címünket
                    if size >= DISCOVERY_MSG.len() && &buf[..DISCOVERY_MSG.len()] == DISCOVERY_MSG {
                        let response = format!("{DISCOVERY_RESPONSE_PREFIX}{ip_string}");
                        let _ = socket.send_to(response.as_bytes(), src);
                        println!("Felfedező kérés a {}-ről -> IP küldve.", src.ip());
                        continue;
                    }

                    last_packet = Instant::now();

                    // Az első datagram jelzi a "kapcsolódást"
                    if !is_live {
                        is_live = true;
                        let w = app_weak.clone();
                        let _ = slint::invoke_from_event_loop(move || {
                            if let Some(app) = w.upgrade() {
                                app.set_is_connected(true);
                                app.set_connection_status_text(
                                    format!("Connected from {}", src.ip()).into(),
                                );
                            }
                        });
                    }

                    // 41 bájtos csomag: magic + controller + kvaternió + orientáció
                    if size >= PACKET_LEN && buf[0] == 0x01 {
                        let steering = f32::from_le_bytes(buf[1..5].try_into().unwrap());
                        let throttle = f32::from_le_bytes(buf[5..9].try_into().unwrap());
                        let brake = f32::from_le_bytes(buf[9..13].try_into().unwrap());
                        let handbrake = f32::from_le_bytes(buf[13..17].try_into().unwrap()) > 0.5;
                        let qw = f32::from_le_bytes(buf[17..21].try_into().unwrap());
                        let qx = f32::from_le_bytes(buf[21..25].try_into().unwrap());
                        let qy = f32::from_le_bytes(buf[25..29].try_into().unwrap());
                        let qz = f32::from_le_bytes(buf[29..33].try_into().unwrap());
                        let pitch = f32::from_le_bytes(buf[33..37].try_into().unwrap());
                        let yaw = f32::from_le_bytes(buf[37..41].try_into().unwrap());

                        println!(
                            "Adat -> Kormany: {steering:.2} | Gaz: {throttle:.2} | Fek: {brake:.2} | Kezifek: {handbrake} | q=({qw:.3},{qx:.3},{qy:.3},{qz:.3}) | pitch={pitch:.1}° | yaw={yaw:.1}°"
                        );

                        let w = app_weak.clone();
                        let _ = slint::invoke_from_event_loop(move || {
                            if let Some(app) = w.upgrade() {
                                app.set_steering_val(format!("{:.0}%", steering * 100.0).into());
                                app.set_throttle_val(format!("{:.0}%", throttle * 100.0).into());
                                app.set_qw_val(format!("{:.4}", qw).into());
                                app.set_qx_val(format!("{:.4}", qx).into());
                                app.set_qy_val(format!("{:.4}", qy).into());
                                app.set_qz_val(format!("{:.4}", qz).into());
                                app.set_pitch_val(format!("{:.1}°", pitch).into());
                                app.set_yaw_val(format!("{:.1}°", yaw).into());
                            }
                        });
                    }
                }
                Err(e)
                    if e.kind() == std::io::ErrorKind::WouldBlock
                        || e.kind() == std::io::ErrorKind::TimedOut =>
                {
                    // Ha már régóta nem érkeznek adatok, offline-ra váltunk
                    if is_live && last_packet.elapsed() > DISCONNECT_TIMEOUT {
                        is_live = false;
                        let w = app_weak.clone();
                        let _ = slint::invoke_from_event_loop(move || {
                            if let Some(app) = w.upgrade() {
                                app.set_is_connected(false);
                                app.set_connection_status_text("No device connected".into());
                                app.set_steering_val("0%".into());
                                app.set_throttle_val("0%".into());
                            }
                        });
                    }
                }
                Err(_) => {}
            }
        }
    });

    app.run()
}