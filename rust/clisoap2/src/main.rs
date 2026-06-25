// cargo run
// http://localhost:8090/?n=10
use actix_web::{web, App, HttpServer, HttpResponse};
use std::collections::HashMap;

fn extract_tag(xml: &str, tag: &str) -> String {
    let open = format!("<{}>", tag);
    let close = format!("</{}>", tag);
    if let Some(start) = xml.find(&open) {
        let start = start + open.len();
        if let Some(end) = xml.find(&close) {
            return xml[start..end].trim().to_string();
        }
    }
    String::new()
}

async fn handler(query: web::Query<HashMap<String, String>>) -> HttpResponse {
    let n = query.get("n").cloned().unwrap_or_else(|| "10".to_string());

    let soap_body = format!(
        r#"<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
      <ubiNum>{}</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>"#,
        n
    );

    let client = reqwest::Client::new();
    let soap_resp = client
        .post("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
        .header("Content-Type", "text/xml; charset=utf-8")
        .body(soap_body)
        .send()
        .await
        .unwrap()
        .text()
        .await
        .unwrap();

    let word = extract_tag(&soap_resp, "NumberToWordsResult");
    let encoded = urlencoding::encode(&word);
    let translate_url = format!(
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q={}",
        encoded
    );

    let json_str = client.get(&translate_url).send().await.unwrap().text().await.unwrap();
    let json: serde_json::Value = serde_json::from_str(&json_str).unwrap();
    let translated = json[0][0][0].as_str().unwrap_or("").to_string();

    HttpResponse::Ok().content_type("text/plain; charset=utf-8").body(translated)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    println!("http://localhost:8090/?n=10");
    HttpServer::new(|| App::new().route("/", web::get().to(handler)))
        .bind("0.0.0.0:8090")?
        .run()
        .await
}
