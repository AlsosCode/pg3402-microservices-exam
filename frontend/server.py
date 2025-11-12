#!/usr/bin/env python3
"""
Simple HTTP server for Pokemon TCG Binder frontend
"""
from http.server import HTTPServer, SimpleHTTPRequestHandler
import sys

class CORSRequestHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

def run(port=3001):
    server_address = ('', port)
    httpd = HTTPServer(server_address, CORSRequestHandler)
    print(f"""
    ╔══════════════════════════════════════════════════════╗
    ║  Pokemon TCG Binder - Frontend Server               ║
    ╠══════════════════════════════════════════════════════╣
    ║  Server running on:  http://localhost:{port}         ║
    ║                                                      ║
    ║  Press Ctrl+C to stop the server                     ║
    ╚══════════════════════════════════════════════════════╝
    """)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped.")
        sys.exit(0)

if __name__ == '__main__':
    run()
