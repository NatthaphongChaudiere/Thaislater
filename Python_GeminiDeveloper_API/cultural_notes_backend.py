import google.generativeai as genai
from flask import Flask, request, jsonify
from flask_cors import CORS
from supabase import create_client, Client
from dotenv import load_dotenv
from datetime import datetime
from zoneinfo import ZoneInfo
import json
import os
import pytz
load_dotenv()

app = Flask(__name__)
CORS(app)  # allow cross-origin for local testing (be careful in production)

# Gemini credentials
Gemini_apiKey = os.getenv("GEMINI_API_KEY", "")

# Supabase credentials
SUPABASE_URL = os.getenv("SUPABASE_URL", "")
SUPABASE_KEY = os.getenv("SUPABASE_KEY", "")
supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
genai.configure(api_key=Gemini_apiKey)



# Obtain user's favorite 
@app.route("/get-favorite", methods = ['GET'])
def get_favorite():
    user_id = request.args.get('user_id')
    response = supabase.table("user_favorites").select("*").eq("user_id", user_id).execute()
    return jsonify(response.data)
# Purpose for delete favorite for particular user
@app.route("/delete-favorite", methods = ['POST'])
def delete_favorite():
    data = request.get_json()
    favorite_id = data.get('current_frame_id')

    if not favorite_id:
        return jsonify({"error": "Missing 'id' in request body"}), 400
    
    delete_response = supabase.table('user_favorites').delete().eq('id', favorite_id).execute()
    return jsonify(delete_response.data)

@app.route("/delete-account", methods = ['POST'])
def delete_account():
    data = request.get_json()
    user_id = data.get("user_id")

    if not user_id:
        return jsonify({"error": "Missing 'id' in request body"}), 400

    supabase.table('user_favorites').delete().eq('user_id', user_id).execute()
    delete_account = supabase.table('users').delete().eq('id', user_id).execute()
    return jsonify(delete_account.data)

# Purpose for create favorite for particular user
@app.route("/create-favorite", methods = ['POST'])
def create_favorite():
    data = request.get_json()
    user_fav_info = {
        "user_id" : data.get('user_id'),
        "original_text" : data.get('original_text'),
        "original_thai_text" : data.get('original_thai_text'),
        "pronounciation" : data.get('pronounciation'),
        "cultural_text" : data.get('cultural_text')
    }

    tz = pytz.timezone("Asia/Bangkok")
    now = datetime.now(tz)

    for key, info in user_fav_info.items():
        if not info:
            return jsonify({"Data Error":f"{key} missing."})
    
    create_user_favorite = supabase.table('user_favorites').insert({
        'user_id': user_fav_info["user_id"], 
        'original': user_fav_info["original_text"], 
        'original_thai': user_fav_info["original_thai_text"],
        'pronounciation': user_fav_info["pronounciation"],
        'thai_cultural_note': user_fav_info["cultural_text"],
        'date': now.date().isoformat(),
        'time': now.time().strftime("%H:%M:%S")
    }).execute()

    result_dict = create_user_favorite.data[0]

    return jsonify(result_dict)

# User's simple sign-in logic [ Purpose for create an account or logging in ]
@app.route("/create-user", methods = ['POST'])
def create_user():
    data = request.get_json()
    username = data.get('username')
    tz = pytz.timezone("Asia/Bangkok")
    now = datetime.now(tz)

    if not username:
        return jsonify({'error': 'Username is required'}), 400
    
    check_if_conflict = supabase.table("users").select("*", count="exact").eq("username", username).execute()
    print(check_if_conflict.count)

    if check_if_conflict.count > 0:
        check_information = supabase.table("users").select("*").eq("username", username).execute()
        return jsonify({"Available": "Username's already created.", "User Info": check_information.data}), 200
    
    supabase.table('users').insert({'username': username, 'date': now.date().isoformat(), 'time': now.time().strftime("%H:%M:%S")}).execute()

    check_information = supabase.table("users").select("*").eq("username", username).execute()
    return jsonify({"Available": "Username has beem created.", "User Info": check_information.data}), 200

# User's using Gemini AI API to seek for Original-Thai, Pronounciation and Thai Cultural note
@app.route("/cultural_note", methods=["POST"])
def call_gemini():
    data = request.get_json(force=True, silent=True) or {}
    phrase = data.get("phrase", "")

    prompt = f"""
    You are a strict JSON generator.
    Always respond with exactly one JSON object only — no backticks, no code blocks, no explanations, no markdown.

    The JSON object MUST contain exactly these keys:
    - "Original" (string): the original phrase
    - "Original-Thai" (string): the translation into Thai
    - "Pronounciation" (string): Thai pronunciation using Latin letters (short form)
    - "Thai Cultural Note" (string): a short cultural note about the phrase, including whether it's polite/appropriate in Thailand

    Input phrase: "{phrase}"

    Valid output format example:
    {{
    "Original": "I love you",
    "Original-Thai": "ฉันรักคุณ",
    "Pronounciation": "Chan rak khun",
    "Thai Cultural Note": "Saying 'I love you' directly is rare in formal situations..."
    }}

    ⚠️ Output only the JSON object. Do not include backticks, 'json', or any other text.
    """

    model = genai.GenerativeModel("gemini-2.5-flash")
    response = model.generate_content(prompt)

    print(response.text)
    
    try:
        result_dict = json.loads(response.text)
    except Exception as e:
        return jsonify({"error": f"Invalid JSON from Gemini: {str(e)}"}), 500

    return jsonify(result_dict)

if __name__ == "__main__":
    # Run on all interfaces to test on emulator or real device
    app.run(host="0.0.0.0", port=5000, debug=True)
