use std::convert::TryFrom;

use qmetaobject::*;

#[derive(QEnum)]
#[repr(u32)]
pub enum AuthState {
    None = 0,
    Loading = 1,
    Authorized = 2,
    Failed = 3,
}

impl Default for AuthState {
    fn default() -> Self {
        AuthState::None
    }
}

impl TryFrom<u32> for AuthState {
    type Error = ();

    fn try_from(value: u32) -> Result<Self, Self::Error> {
        Ok(match value {
            0 => Self::None,
            1 => Self::Loading,
            2 => Self::Authorized,
            3 => Self::Failed,
            _ => return Err(()),
        })
    }
}
