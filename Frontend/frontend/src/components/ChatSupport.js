import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useAuth } from '../context/AuthContext';

const ChatSupport = () => {
    const { userId } = useAuth();
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { sender: 'SYSTEM', content: 'Hello! How can I help you today? Ask me a question.' }
    ]);
    const [input, setInput] = useState('');
    const [stompClient, setStompClient] = useState(null);
    const [isLiveChat, setIsLiveChat] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages, isOpen]);

    useEffect(() => {
        if (!userId) return;

        const socket = new SockJS('http://localhost:8084/ws');
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Chat Connected');
                client.subscribe(`/topic/chat/${userId}`, (message) => {
                    if (message.body) {
                        const body = JSON.parse(message.body);
                        setMessages(prev => [...prev, { sender: body.sender, content: body.content }]);
                    }
                });
            },
        });

        client.activate();
        setStompClient(client);

        return () => {
            client.deactivate();
        };
    }, [userId]);

    const sendMessage = (e) => {
        e.preventDefault();
        if (input.trim() && stompClient && stompClient.connected) {
            const message = {
                sender: 'USER',
                content: input,
                userId: userId
            };

            const destination = isLiveChat ? '/app/chat/to-admin' : '/app/chat';

            stompClient.publish({
                destination: destination,
                body: JSON.stringify(message)
            });
            setMessages(prev => [...prev, { sender: 'USER', content: input }]);
            setInput('');
        }
    };

    if (!userId) return null;

    return (
        <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 1000 }}>
            {!isOpen && (
                <button 
                    onClick={() => setIsOpen(true)}
                    style={{
                        width: '60px',
                        height: '60px',
                        borderRadius: '50%',
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        cursor: 'pointer',
                        boxShadow: '0 4px 8px rgba(0,0,0,0.2)',
                        fontSize: '24px'
                    }}
                >
                    💬
                </button>
            )}

            {isOpen && (
                <div style={{
                    width: '350px',
                    height: '500px',
                    backgroundColor: 'white',
                    borderRadius: '10px',
                    boxShadow: '0 5px 15px rgba(0,0,0,0.3)',
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'hidden'
                }}>
                    <div style={{
                        padding: '15px',
                        backgroundColor: '#007bff',
                        color: 'white',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <span style={{ fontWeight: 'bold' }}>
                            {isLiveChat ? 'Live Admin Support' : 'Automated Support'}
                        </span>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <button
                                onClick={() => setIsLiveChat(!isLiveChat)}
                                style={{
                                    background: 'white',
                                    border: 'none',
                                    color: '#007bff',
                                    cursor: 'pointer',
                                    fontSize: '12px',
                                    padding: '2px 8px',
                                    borderRadius: '4px'
                                }}
                            >
                                {isLiveChat ? 'Switch to Bot' : 'Chat with Admin'}
                            </button>
                            <button
                                onClick={() => setIsOpen(false)}
                                style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer', fontSize: '16px' }}
                            >
                                ✕
                            </button>
                        </div>
                    </div>

                    <div style={{
                        flex: 1,
                        padding: '15px',
                        overflowY: 'auto',
                        backgroundColor: '#f8f9fa',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '10px'
                    }}>
                        {messages.map((msg, index) => (
                            <div key={index} style={{
                                alignSelf: msg.sender === 'USER' ? 'flex-end' : 'flex-start',
                                backgroundColor: msg.sender === 'USER' ? '#007bff' : '#e9ecef',
                                color: msg.sender === 'USER' ? 'white' : 'black',
                                padding: '10px 15px',
                                borderRadius: '15px',
                                maxWidth: '80%',
                                wordWrap: 'break-word'
                            }}>
                                {msg.content}
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>

                    <form onSubmit={sendMessage} style={{
                        padding: '15px',
                        borderTop: '1px solid #dee2e6',
                        display: 'flex',
                        gap: '10px'
                    }}>
                        <input
                            type="text"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="Type a question..."
                            style={{
                                flex: 1,
                                padding: '10px',
                                borderRadius: '20px',
                                border: '1px solid #ced4da',
                                outline: 'none'
                            }}
                        />
                        <button 
                            type="submit"
                            disabled={!input.trim()}
                            style={{
                                backgroundColor: '#007bff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '20px',
                                padding: '0 20px',
                                cursor: 'pointer'
                            }}
                        >
                            Send
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
};

export default ChatSupport;
