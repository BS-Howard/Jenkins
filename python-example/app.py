from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello():
    return "Hello from Python service!!"

@app.route('/healthz')
def healthz():
    return "OK", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
