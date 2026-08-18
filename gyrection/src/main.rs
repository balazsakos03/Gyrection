slint::include_modules!();

use std::io::Read;
use std::net::TcpListener;
use std::thread;

fn main() -> Result<(), slint::PlatformError> {
    let app = AppWindow::new()?;

    // Hivatkozás a Slint ablakra, amit átadunk a háttérszálnak
    let app_weak = app.as_weak();

    app.on_connect_clicked(move || {
        println!("A szerver már fut és figyeli a 9999-es portot.");
    });

    // Háttérszál indítása a TCP szerver számára
    thread::spawn(move || {
        let listener = TcpListener::bind("127.0.0.1:9999").expect("Nem sikerült elindítani a TCP szervert!");
        println!("TCP szerver elindult a 127.0.0.1:9999 címen.");

        for stream in listener.incoming() {
            match stream {
                Ok(mut stream) => {
                    println!("Telefon sikeresen csatlakozott USB-n keresztül!");

                    // Helyes módszer: a Weak referenciát adjuk át, és a fő szálon upgrade-eljük
                    let app_weak_conn = app_weak.clone();
                    let _ = slint::invoke_from_event_loop(move || {
                        if let Some(app) = app_weak_conn.upgrade() {
                            app.set_is_connected(true);
                            app.set_connection_status_text("Connected".into());
                        }
                    });

                    let mut buffer = [0u8; 17];

                    loop {
                        match stream.read_exact(&mut buffer) {
                            Ok(_) => {
                                if buffer[0] == 0x01 {
                                    let steering = f32::from_le_bytes(buffer[1..5].try_into().unwrap());
                                    let throttle = f32::from_le_bytes(buffer[5..9].try_into().unwrap());
                                    let brake = f32::from_le_bytes(buffer[9..13].try_into().unwrap());
                                    let handbrake = f32::from_le_bytes(buffer[13..17].try_into().unwrap()) > 0.5f32;

                                    println!(
                                        "Adat érkezett -> Kormány: {:.2}, Gáz: {:.2}, Fék: {:.2}, Kézifék: {}",
                                        steering, throttle, brake, handbrake
                                    );

                                    // Formázott értékek a Slint felületnek
                                    let steering_str = format!("{:.0}%", steering * 100.0);
                                    let throttle_str = format!("{:.0}%", throttle * 100.0);

                                    // Helyes módszer: a Weak referenciát klónozzuk a closure-be
                                    let app_weak_data = app_weak.clone();
                                    let _ = slint::invoke_from_event_loop(move || {
                                        if let Some(app) = app_weak_data.upgrade() {
                                            app.set_steering_val(steering_str.into());
                                            app.set_throttle_val(throttle_str.into());
                                        }
                                    });
                                }
                            }
                            Err(_) => {
                                println!("A kapcsolat megszakadt a telefonnal.");
                                
                                // Helyes módszer: Weak referenciával frissítünk lekapcsoláskor is
                                let app_weak_disc = app_weak.clone();
                                let _ = slint::invoke_from_event_loop(move || {
                                    if let Some(app) = app_weak_disc.upgrade() {
                                        app.set_is_connected(false);
                                        app.set_connection_status_text("No device connected".into());
                                        app.set_steering_val("0%".into());
                                        app.set_throttle_val("0%".into());
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
                Err(e) => {
                    println!("Hiba a kapcsolat elfogadásakor: {}", e);
                }
            }
        }
    });

    app.run()
}