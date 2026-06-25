// sudo apt install libicu-dev
// g++ -o conintl conintl.cpp `pkg-config --cflags --libs icu-uc icu-i18n` -lpthread
// ./conintl
// http://localhost:8081/?n=10
#include <iostream>
#include <string>
#include <thread>
#include <unicode/rbnf.h>
#include <unicode/unistr.h>
#include <netinet/in.h>
#include <unistd.h>

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
    int n = std::stoi(parseQueryParam(query, "n"));

    UErrorCode status = U_ZERO_ERROR;
    icu::Locale locale("es");
    icu::RuleBasedNumberFormat fmt(icu::URBNF_SPELLOUT, locale, status);
    icu::UnicodeString result;
    fmt.format(static_cast<double>(n), result);
    std::string output;
    result.toUTF8String(output);

    std::string response =
        "HTTP/1.0 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\n" + output;
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
