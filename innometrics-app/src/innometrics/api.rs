#![allow(non_snake_case)]

use qmetaobject::*;

use innometrics_api::prelude::*;

use crate::*;

#[allow(non_snake_case)]
#[derive(Default, QObject)]
pub struct Api {
    base: qt_base_class!(trait QObject),

    // DTO factory functions
    createAuthRequest: qt_method!(fn(&self, email: QString, password: QString, project_id: QString) -> QVariant),

    // API methods
    login: qt_method!(fn(&self, req: QmlAuthRequest)),
    loginSuccess: qt_signal!(token: QString),
    loginFail: qt_signal!(status: u32),
}

// there's no shared data anyway, only signals and methods which are Sync according to Qt.
unsafe impl std::marker::Sync for Api {}

impl Api {
    // DTO factory functions
    fn createAuthRequest(&self, email: QString, password: QString, project_id: QString) -> QVariant {
        QmlAuthRequest {
            email,
            password,
            project_id,
        }.to_qvariant()
    }

    // API methods
    fn login(&self, req: QmlAuthRequest) {
        let callback = qmetaobject::queued_callback(move |(this, result): (&Api, ApiResult<TokenResponse>)| {
            match result {
                Ok(token) => this.loginSuccess(token.token.into()),
                Err(_err) => this.loginFail(0)
            }
        });

        let this = unsafe { std::mem::transmute::<_, &'static Api>(self) };

        std::thread::spawn(move || {
            let res = Adapter::instance().login(req.into());
            callback((this, res));
        });
    }
}

#[derive(Clone, Debug, Default, QGadget)]
pub struct QmlAuthRequest {
    pub email: qt_property!(QString),
    pub password: qt_property!(QString),
    pub project_id: qt_property!(QString),
}

impl From<QmlAuthRequest> for AuthRequest {
    fn from(it: QmlAuthRequest) -> Self {
        AuthRequest {
            email: it.email.to_string(),
            password: it.password.to_string(),
            project_id: it.project_id.to_string()
        }
    }
}