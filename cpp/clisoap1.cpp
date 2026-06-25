// sudo apt install libcurl4-openssl-dev
// g++ -o clisoap1 clisoap1.cpp -lcurl -lpthread
// ./clisoap1
// http://localhost:8081/?n=10
#include <iostream>
#include <string>
#include <sstream>
#include <thread>
#include <curl/curl.h>
#include <netinet/in.h>
#include <unistd.h>

static size_t writeCb(void* ptr, size_t size, size_t nmemb, std::string* s) {
    s->append(static_cast<char*>(ptr), size * nmemb);
    return size * nmemb;
}

std::string soapPost(const std::string& n) {
    std::string body =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        "<soap:Body>"
        "<NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">"
        "<ubiNum>" + n + "</ubiNum>"
        "</NumberToWords></soap:Body></soap:Envelope>";

    std::string response;
    CURL* curl = curl_easy_init();
    curl_slist* hdrs = curl_slist_append(nullptr, "Content-Type: text/xml; charset=utf-8");
    curl_easy_setopt(curl, CURLOPT_URL,
        "https://www.dataaccess.com/webservicesserver/NumberConversion.wso");
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, body.c_str());
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, hdrs);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writeCb);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
    curl_easy_perform(curl);
    curl_easy_cleanup(curl);
    curl_slist_free_all(hdrs);
    return response;
}

std::string extractTag(const std::string& xml, const std::string& tag) {
    std::string open = "<" + tag + ">";
    std::string close = "</" + tag + ">";
    size_t s = xml.find(open);
    if (s == std::string::npos) return "";
    s += open.size();
    size_t e = xml.find(close, s);
    return xml.substr(s, e - s);
}

std::string parseQueryParam(const std::string& query, const std::string& key) {
    size_t pos = query.find(key + "=");
    if (pos == std::string::npos) return "10";
    pos += key.size() + 1;
    size_t end = query.find('&', pos);
    return query.substr(pos, end == std::string::npos ? end : end - pos);
}

void handleClient(int sock) {
    char buf[4096] = {};
    read(sock, buf, sizeof(buf) - 1);
    std::string req(buf);

    std::string query;
    size_t qPos = req.find('?');
    if (qPos != std::string::npos) {
        size_t spPos = req.find(' ', qPos);
        query = req.substr(qPos + 1, spPos - qPos - 1);
    }
    std::string n = parseQueryParam(query, "n");
    std::string result = extractTag(soapPost(n), "NumberToWordsResult");

    std::string response =
        "HTTP/1.0 200 OK\r\nContent-Type: text/plain\r\n\r\n" + result;
    write(sock, response.c_str(), response.size());
    close(sock);
}

int main() {
    int srv = socket(AF_INET, SOCK_STREAM, 0);
    int opt = 1;
    setsockopt(srv, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(8081);
    addr.sin_addr.s_addr = INADDR_ANY;
    bind(srv, (sockaddr*)&addr, sizeof(addr));
    listen(srv, 10);
    std::cout << "http://localhost:8081/?n=10\n";
    while (true) {
        int client = accept(srv, nullptr, nullptr);
        std::thread(handleClient, client).detach();
    }
}
