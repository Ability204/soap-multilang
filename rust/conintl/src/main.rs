// cargo run
// http://localhost:8090/?n=10
use actix_web::{web, App, HttpServer, HttpResponse};
use std::collections::HashMap;

fn to_words_es(n: i64) -> String {
    let ones = ["", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete",
                "ocho", "nueve", "diez", "once", "doce", "trece", "catorce",
                "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"];
    let tens = ["", "", "veinte", "treinta", "cuarenta", "cincuenta",
                "sesenta", "setenta", "ochenta", "noventa"];
    let hundreds = ["", "ciento", "doscientos", "trescientos", "cuatrocientos",
                    "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"];

    if n == 0 { return "cero".to_string(); }
    if n < 20 { return ones[n as usize].to_string(); }
    if n < 100 {
        if n % 10 == 0 { return tens[(n / 10) as usize].to_string(); }
        return format!("{} y {}", tens[(n / 10) as usize], ones[(n % 10) as usize]);
    }
    if n < 1000 {
        if n == 100 { return "cien".to_string(); }
        let rest = to_words_es(n % 100);
        if rest.is_empty() { return hundreds[(n / 100) as usize].to_string(); }
        return format!("{} {}", hundreds[(n / 100) as usize], rest);
    }
    n.to_string()
}

async fn handler(query: web::Query<HashMap<String, String>>) -> HttpResponse {
    let n: i64 = query
        .get("n")
        .and_then(|v| v.parse().ok())
        .unwrap_or(10);
    HttpResponse::Ok()
        .content_type("text/plain; charset=utf-8")
        .body(to_words_es(n))
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    println!("http://localhost:8090/?n=10");
    HttpServer::new(|| App::new().route("/", web::get().to(handler)))
        .bind("0.0.0.0:8090")?
        .run()
        .await
}
