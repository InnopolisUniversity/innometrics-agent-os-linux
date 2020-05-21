//! Date-time formatter module for serde which matches format of Innometrics API.
use chrono::{DateTime, Utc};
use serde::{self, Deserialize, Deserializer, Serializer};

const FORMAT: &'static str = "%Y-%m-%dT%H:%M:%S%.f%z";

pub fn serialize<S>(date: &DateTime<Utc>, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
{
    let s = date.format(FORMAT).to_string();
    serializer.serialize_str(&s)
}

pub fn deserialize<'de, D>(deserializer: D) -> Result<DateTime<Utc>, D::Error>
    where
        D: Deserializer<'de>,
{
    let s = <&str>::deserialize(deserializer)?;
    DateTime::parse_from_str(s, FORMAT)
        .map(Into::into)
        .map_err(serde::de::Error::custom)
}
