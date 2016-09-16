package scripts.io.util.java_websocket.server;

import scripts.io.util.java_websocket.WebSocketAdapter;
import scripts.io.util.java_websocket.WebSocketImpl;
import scripts.io.util.java_websocket.drafts.Draft;

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class DefaultWebSocketServerFactory implements WebSocketServer.WebSocketServerFactory {
	@Override
	public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d, Socket s ) {
		return new WebSocketImpl( a, d );
	}
	@Override
	public WebSocketImpl createWebSocket(WebSocketAdapter a, List<Draft> d, Socket s ) {
		return new WebSocketImpl( a, d );
	}
	@Override
	public SocketChannel wrapChannel(SocketChannel channel, SelectionKey key ) {
		return (SocketChannel) channel;
	}
}