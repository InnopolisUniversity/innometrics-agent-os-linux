use reqwest::blocking;

use crate::api::*;

/// Extension trait for convenient authentication on `RequestBuilder`.
pub trait RequestBuilderAuthenticatorExt {
    fn authenticate(self, authenticator: &dyn RequestAuthenticator) -> Self;
}

impl RequestBuilderAuthenticatorExt for blocking::RequestBuilder {
    fn authenticate(self, authenticator: &dyn RequestAuthenticator) -> Self {
        authenticator.authenticate(self)
    }
}
