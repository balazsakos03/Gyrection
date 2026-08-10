slint::include_modules!();

fn main() -> Result<(), slint::PlatformError> {
    let app = AppWindow::new()?;

    app.on_connect_clicked(|| {
        println!("Connect clicked!");
    });

    app.run()
}