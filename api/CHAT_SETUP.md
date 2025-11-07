# Chat Endpoint Setup

The AI Advisor chat feature requires the Anthropic Claude API.

## Configuration

1. **Get an API key from Anthropic:**
   - Visit https://console.anthropic.com/
   - Create an account or sign in
   - Go to API Keys section
   - Create a new API key

2. **Add the API key to your environment:**

   ```bash
   cd api
   cp .env.example .env
   # Edit .env and add your key:
   ANTHROPIC_API_KEY=sk-ant-api03-...
   ```

3. **Install dependencies** (if not already installed):
   ```bash
   pip install -r requirements.txt
   ```

4. **Start the API server:**
   ```bash
   python -m uvicorn app.main:app --host 0.0.0.0 --port 2020 --reload
   ```

## Using the Chat Endpoint

### Endpoint
```
POST /api/chat
```

### Request
```json
{
  "message": "What should I look for during a spring hive inspection?"
}
```

### Response
```json
{
  "id": "uuid-here",
  "content": "During a spring hive inspection, you should look for...",
  "role": "assistant",
  "timestamp": "2025-11-07T10:30:00"
}
```

### Example with curl
```bash
curl -X POST http://localhost:2020/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How do I identify varroa mites?"}'
```

## Features

The AI advisor provides expert guidance on:

- **Hive Management:** Inspection techniques, queen assessment, colony health
- **Pest Control:** Varroa mites, small hive beetles, wax moths identification and treatment
- **Disease Management:** Foulbrood, nosema, and other bee diseases
- **Seasonal Tasks:** Spring buildup, summer flows, fall preparation, winter care
- **Equipment:** Hive configurations, tool selection, best practices
- **Honey Production:** Harvesting, extraction, processing
- **Swarm Management:** Prevention and capture techniques

## Testing

The mobile app AI Advisor tab will automatically connect to this endpoint when you send messages.

Make sure:
1. Backend is running on `http://localhost:2020` (or update `ApiConfig` in mobile app)
2. ANTHROPIC_API_KEY is set in `.env`
3. Internet connection is available (Claude API requires internet)

## Troubleshooting

**"ANTHROPIC_API_KEY not configured" error:**
- Make sure `.env` file exists in the `api` directory
- Verify the API key is correctly set
- Restart the server after adding the key

**"Failed to get AI response" error:**
- Check your internet connection
- Verify API key is valid (test at https://console.anthropic.com/)
- Check Anthropic API status page for outages

**Rate limiting:**
- Free tier has rate limits (varies by plan)
- Consider implementing caching for common questions
- Upgrade plan if needed for higher usage

## Cost Considerations

- Claude 3.5 Sonnet pricing: ~$3 per million input tokens, ~$15 per million output tokens
- Average conversation: ~500 input + 500 output tokens = $0.009
- 1000 conversations ≈ $9
- Monitor usage at https://console.anthropic.com/

## Future Enhancements

Potential improvements:
- [ ] Conversation history (store previous messages for context)
- [ ] Hive-specific context (pass current hive data to Claude)
- [ ] Image analysis integration (combine with photo upload feature)
- [ ] Caching common questions to reduce API calls
- [ ] Rate limiting per user
- [ ] Alternative models (Claude Haiku for faster/cheaper responses)
