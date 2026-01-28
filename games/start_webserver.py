#!/usr/bin/env python3
import http.server
import socketserver
import os

# Change to the games directory
os.chdir('/Users/yu69yj/Documents/workspace/Chapter_Games/games')

PORT = 10000

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        self.send_header('Pragma', 'no-cache')
        self.send_header('Expires', '0')
        super().end_headers()

with socketserver.TCPServer(("", PORT), MyHTTPRequestHandler) as httpd:
    print(f"Server running at http://localhost:{PORT}/")
    print(f"Open http://localhost:{PORT}/webclient.html to play the game")
    httpd.serve_forever()
