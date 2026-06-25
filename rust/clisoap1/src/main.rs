// cargo run
// http://localhost:8090/?n=10
use actix_web::{web, App, HttpServer, HttpResponse};
use std::collections::HashMap;

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
    let response = client
        .post("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
        .header("Content-Type", "text/xml; charset=utf-8")
        .body(soap_body)
        .send()
        .await
        .unwrap()
        .text()
        .await
        .unwrap();

    let tag = "NumberToWordsResult";
    let open = format!("<{}>", tag);
    let close = format!("</{}>", tag);
    let start = response.find(&open).unwrap_or(0) + open.len();
    let end = response.find(&close).unwrap_or(start);
    let result = response[start..end].trim().to_string();

    HttpResponse::Ok().body(result)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    println!("http://localhost:8090/?n=10");
    HttpServer::new(|| App::new().route("/", web::get().to(handler)))
        .bind("0.0.0.0:8090")?
        .run()
        .await
}
