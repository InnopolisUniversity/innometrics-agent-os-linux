use reqwest::blocking;

use crate::api::*;
use crate::dto::*;

/// Request authenticator based on adding HTTP `Token` header to the request.
#[derive(Clone, Debug)]
pub struct TokenHeaderAuthenticator {
    pub token: String
}

impl TokenHeaderAuthenticator {
    pub fn new(token: String) -> Self {
        TokenHeaderAuthenticator {
            token
        }
    }

    pub fn boxed(self) -> Box<Self> {
        Box::new(self)
    }
}

impl From<TokenResponse> for TokenHeaderAuthenticator {
    fn from(res: TokenResponse) -> Self {
        Self::new(res.token)
    }
}

impl RequestAuthenticator for TokenHeaderAuthenticator {
    fn authenticate(&self, req: blocking::RequestBuilder) -> blocking::RequestBuilder {
        req.header("Token", &self.token)
    }
}
