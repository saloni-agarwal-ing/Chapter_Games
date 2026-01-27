#!/usr/bin/env python3
"""
Simple WebSocket test client to diagnose Drop Game server connection
"""
import asyncio
import websockets
import json
import ssl

async def test_websocket_connection():
    uri = "wss://chapter-games-websockify.onrender.com"
    print(f"🔍 Testing WebSocket connection to: {uri}")

    # Create SSL context that doesn't verify certificates (for testing only)
    ssl_context = ssl.create_default_context()
    ssl_context.check_hostname = False
    ssl_context.verify_mode = ssl.CERT_NONE

    try:
        async with websockets.connect(uri, ssl=ssl_context) as websocket:
            print("✅ WebSocket connection established!")

            # Send join message
            join_message = {
                "type": "join",
                "playerName": "PythonTestClient"
            }
            await websocket.send(json.dumps(join_message))
            print(f"📤 Sent join message: {join_message}")

            # Wait for response
            try:
                response = await asyncio.wait_for(websocket.recv(), timeout=10.0)
                print(f"📥 Received response: {response}")

                # Parse response
                data = json.loads(response)
                if data.get("type") == "welcome":
                    print(f"✅ Successfully joined as player {data.get('playerId')}")

                    # Wait for game state
                    game_state = await asyncio.wait_for(websocket.recv(), timeout=5.0)
                    print(f"📥 Received game state: {game_state}")

                    state_data = json.loads(game_state)
                    if state_data.get("type") == "gameState":
                        print("✅ Game server is working correctly!")
                        print(f"   - World size: {state_data.get('worldWidth')}x{state_data.get('worldHeight')}")
                        print(f"   - Players: {state_data.get('playerNames', {})}")
                        print(f"   - Scores: {state_data.get('scoreboard', {})}")
                        print(f"   - Drops: {len(state_data.get('drops', []))} active drops")
                        return True

            except asyncio.TimeoutError:
                print("⏰ Timeout waiting for server response")
                return False

    except websockets.exceptions.ConnectionClosed as e:
        print(f"❌ WebSocket connection closed: {e}")
        return False
    except websockets.exceptions.InvalidURI as e:
        print(f"❌ Invalid WebSocket URI: {e}")
        return False
    except Exception as e:
        print(f"❌ WebSocket connection failed: {e}")
        return False

async def main():
    print("🎮 Drop Game WebSocket Connection Test")
    print("=====================================")

    success = await test_websocket_connection()

    if success:
        print("\n✅ DIAGNOSIS: Server is working correctly")
        print("   The issue is likely in your desktop client configuration")
        print("   Check that you're using the correct server URI parameter")
    else:
        print("\n❌ DIAGNOSIS: Server connection failed")
        print("   The websockify proxy is running but Java server may not be working")
        print("   Check Render service logs for Java server errors")

if __name__ == "__main__":
    asyncio.run(main())
