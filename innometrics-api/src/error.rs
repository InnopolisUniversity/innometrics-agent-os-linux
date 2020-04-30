use std::error::Error as StdError;
use std::fmt;

use serde_json::error::Error as JsonError;

// quick_error! {
//     #[derive(Debug)]
//     pub enum ServiceError {
//         /// Network connectivity or other network-related issue.
//         Network(inner: Box<dyn StdError>) {}
//         /// Server returned 401 Unauthorized response
//         Unauthorized() {
//             from()
//         }
//         /// Other
//         Other(err: Box<dyn StdError>) {}
//     }
// }

#[derive(Debug)]
pub enum ServiceError {
    Unauthorized,
    Network { inner: Box<dyn StdError> },
    Server { inner: Box<dyn StdError> },
    Serde { inner: JsonError },
}

impl fmt::Display for ServiceError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            ServiceError::Unauthorized => { "Unauthorized".fmt(f)?; }
            ServiceError::Network { inner } => {
                f.debug_struct("Network")
                    .field("inner", inner)
                    .finish()?;
            }
            ServiceError::Server { inner } => {
                f.debug_struct("Server")
                    .field("inner", inner)
                    .finish()?;
            }
            ServiceError::Serde { inner } => {
                f.debug_struct("Serde")
                    .field("inner", inner)
                    .finish()?;
            }
        }
        Ok(())
    }
}

impl From<JsonError> for ServiceError {
    fn from(it: JsonError) -> Self {
        Self::Serde { inner: it }
    }
}
