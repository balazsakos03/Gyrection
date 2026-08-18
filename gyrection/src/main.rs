slint::include_modules!();

use std::net::{IpAddr, Ipv4Addr, UdpSocket};
use std::thread;
use std::time::{Duration, Instant};

const UDP_PORT: u16 = 9999;
const PACKET_LEN: usize = 41;
const DISCONNECT_TIMEOUT: Duration = Duration::from_millis(1500);

/// The discovery message sent by the phone (broadcast). When we see it,
/// the phone knows the PC server is here, so we reply with our IP address.
const DISCOVERY_MSG: &[u8] = b"GYRECTION_DISCOVERY";
const DISCOVERY_RESPONSE_PREFIX: &str = "GYRECTION_IP ";

/// Finds the machine's local (LAN) IP address using a non-sending UDP "probe",
/// so the UI can show where the phone should send data.
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

    // The PC's local address the phone should send to
    let ip = local_ipv4();
    let ip_string = ip.to_string();
    let server_info = format!("WiFi UDP | phone sends to {ip}:{UDP_PORT}");
    app.set_server_status_text(server_info.clone().into());
    println!("{server_info}");

    // UDP server thread: continuously receives the phone's datagrams
    thread::spawn(move || {
        let socket = UdpSocket::bind((Ipv4Addr::UNSPECIFIED, UDP_PORT))
            .unwrap_or_else(|e| panic!("Failed to start the UDP server on port {UDP_PORT}: {e}"));
        socket
            .set_read_timeout(Some(Duration::from_millis(200)))
            .expect("Failed to set the read timeout.");
        println!("UDP server listening on 0.0.0.0:{UDP_PORT} (WiFi).");

        let mut last_packet = Instant::now();
        let mut is_live = false;

        // --- Xbox 360 virtual controller via ViGEmBus ---
        let mut xbox_target: Option<vigem_client::Xbox360Wired<vigem_client::Client>> = None;
        match vigem_client::Client::connect() {
            Ok(client) => {
                let mut t = vigem_client::Xbox360Wired::new(client, vigem_client::TargetId::XBOX360_WIRED);
                match t.plugin() {
                    Ok(_) => {
                        let _ = t.wait_ready();
                        println!("Xbox 360 virtual controller emulated (ViGEmBus).");
                        xbox_target = Some(t);
                        let w = app_weak.clone();
                        let _ = slint::invoke_from_event_loop(move || {
                            if let Some(app) = w.upgrade() {
                                app.set_virtual_controller_active(true);
                            }
                        });
                    }
                    Err(e) => println!(
                        "Xbox 360 plugin failed: {e}. Install the ViGEmBus driver, then restart."
                    ),
                }
            }
            Err(e) => println!(
                "ViGEm client connect failed: {e}. Install the ViGEmBus driver, then restart."
            ),
        }

        let mut buf = [0u8; 64];
        loop {
            match socket.recv_from(&mut buf) {
                Ok((size, src)) => {
                    // Broadcast discovery: the phone is looking for the PC -> send our IP
                    if size >= DISCOVERY_MSG.len() && &buf[..DISCOVERY_MSG.len()] == DISCOVERY_MSG {
                        let response = format!("{DISCOVERY_RESPONSE_PREFIX}{ip_string}");
                        let _ = socket.send_to(response.as_bytes(), src);
                        println!("Discovery request from {} -> IP sent.", src.ip());
                        continue;
                    }

                    last_packet = Instant::now();

                    // The first datagram marks the connection
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

                    // 41-byte packet: magic + controller + quaternion + orientation
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
                            "Data -> Steering: {steering:.2} | Throttle: {throttle:.2} | Brake: {brake:.2} | Handbrake: {handbrake} | q=({qw:.3},{qx:.3},{qy:.3},{qz:.3}) | pitch={pitch:.1}° | yaw={yaw:.1}°"
                        );

                        let w = app_weak.clone();
                        let _ = slint::invoke_from_event_loop(move || {
                            if let Some(app) = w.upgrade() {
                                app.set_steering_val(format!("{:.0}%", steering * 100.0).into());
                                app.set_throttle_val(format!("{:.0}%", throttle * 100.0).into());
                                app.set_brake_val(format!("{:.0}%", brake * 100.0).into());
                                app.set_handbrake_active(handbrake);
                                // Quaternion values are parsed and logged (see above) for debugging.
                                // If the debug UI is re-enabled in app-window.slint, uncomment:
                                // app.set_qw_val(format!("{:.4}", qw).into());
                                // app.set_qx_val(format!("{:.4}", qx).into());
                                // app.set_qy_val(format!("{:.4}", qy).into());
                                // app.set_qz_val(format!("{:.4}", qz).into());
                                app.set_pitch_val(format!("{:.1}°", pitch).into());
                                app.set_yaw_val(format!("{:.1}°", yaw).into());
                            }
                        });

                        // Forward the received data to the virtual Xbox 360 controller
                        if let Some(t) = xbox_target.as_mut() {
                            let buttons = if handbrake {
                                vigem_client::XButtons!(A)
                            } else {
                                vigem_client::XButtons(0)
                            };
                            let gamepad = vigem_client::XGamepad {
                                buttons,
                                left_trigger: (brake.clamp(0.0, 1.0) * 255.0) as u8,
                                right_trigger: (throttle.clamp(0.0, 1.0) * 255.0) as u8,
                                thumb_lx: (steering.clamp(-1.0, 1.0) * 32767.0) as i16,
                                thumb_ly: 0,
                                thumb_rx: 0,
                                thumb_ry: 0,
                            };
                            let _ = t.update(&gamepad);
                        }
                    }
                }
                Err(e)
                    if e.kind() == std::io::ErrorKind::WouldBlock
                        || e.kind() == std::io::ErrorKind::TimedOut =>
                {
                    // If no data has arrived for a while, switch to offline
                    if is_live && last_packet.elapsed() > DISCONNECT_TIMEOUT {
                        is_live = false;
                        let w = app_weak.clone();
                        let _ = slint::invoke_from_event_loop(move || {
                            if let Some(app) = w.upgrade() {
                                app.set_is_connected(false);
                                app.set_connection_status_text("No device connected".into());
                                app.set_steering_val("0%".into());
                                app.set_throttle_val("0%".into());
                                app.set_brake_val("0%".into());
                                app.set_handbrake_active(false);
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