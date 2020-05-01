pub fn init() {
    env_logger::from_env(
        env_logger::Env::default()
            .default_filter_or("warn")
    ).init();
    qmetaobject::log::init_qt_to_rust();
}
