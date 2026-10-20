import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useAuth } from '../context/AuthContext';

const AdminChat = ({ users = [] }) => {
    const { role } = useAuth();
    const [activeChats, setActiveChats] = useState({});
    const [selectedUser, setSelectedUser] = useState(null);
    const [input, setInput] = useState('');
    const [stompClient, setStompClient] = useState(null);
    const [connectionStatus, setConnectionStatus] = useState('Connecting...');
    const messagesEndRef = useRef(null);

    // Sort users: Active chats first, then alphabetical
    const sortedUsers = [...users].sort((a, b) => {
        const aActive = activeChats[a.id] && activeChats[a.id].length > 0;
        const bActive = activeChats[b.id] && activeChats[b.id].length > 0;
        if (aActive && !bActive) return -1;
        if (!aActive && bActive) return 1;
        return a.firstName.localeCompare(b.firstName);
    });

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [activeChats, selectedUser]);

    useEffect(() => {
        if (role !== 'ADMIN') return;

        const socket = new SockJS('http://localhost:8084/ws');
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Admin Chat Connected');
                setConnectionStatus('Connected');
                client.subscribe('/topic/admin/messages', (message) => {
                    if (message.body) {
                        const body = JSON.parse(message.body);
                        handleIncomingMessage(body);
                    }
                });
            },
            onDisconnect: () => {
                setConnectionStatus('Disconnected');
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                setConnectionStatus('Error');
            }
        });

        client.activate();
        setStompClient(client);

        return () => {
            client.deactivate();
        };
    }, [role]);

    const handleIncomingMessage = (message) => {
        console.log("Admin received message:", message); // Debug log
        setActiveChats(prev => {
            const senderId = message.userId;
            if (!senderId) return prev; // Safety check

            const userChat = prev[senderId] || [];
            return {
                ...prev,
                [senderId]: [...userChat, { sender: 'USER', content: message.content }]
            };
        });
    };

    const sendMessage = (e) => {
        e.preventDefault();
        if (input.trim() && stompClient && stompClient.connected && selectedUser) {
            const message = {
                sender: 'ADMIN',
                content: input,
                userId: selectedUser
            };

            stompClient.publish({
                destination: '/app/chat/to-user',
                body: JSON.stringify(message)
            });

            setActiveChats(prev => ({
                ...prev,
                [selectedUser]: [...(prev[selectedUser] || []), { sender: 'ADMIN', content: input }]
            }));
            setInput('');
        }
    };

    if (role !== 'ADMIN') return null;

    return (
        <div style={{
            display: 'flex',
            height: '500px',
            width: '800px',
            margin: '20px auto',
            backgroundColor: 'white',
            borderRadius: '10px',
            boxShadow: '0 5px 15px rgba(0,0,0,0.1)',
            overflow: 'hidden',
            border: '1px solid #dee2e6',
            color: '#333', // Force dark text color for the whole component
            fontFamily: 'sans-serif'
        }}>
            {/* Sidebar - User List */}
            <div style={{
                width: '250px',
                backgroundColor: '#f8f9fa',
                borderRight: '1px solid #dee2e6',
                overflowY: 'auto',
                color: '#333' // Force dark text
            }}>
                <div style={{
                    padding: '15px',
                    fontWeight: 'bold',
                    borderBottom: '1px solid #dee2e6',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    backgroundColor: '#fff',
                    color: '#333'
                }}>
                    <span>Users</span>
                    <span style={{ fontSize: '10px', color: connectionStatus === 'Connected' ? 'green' : 'red' }}>● {connectionStatus}</span>
                </div>

                {sortedUsers.map(user => {
                    const hasMessages = activeChats[user.id] && activeChats[user.id].length > 0;
                    const isSelected = selectedUser === user.id;
                    return (
                        <div
                            key={user.id}
                            onClick={() => setSelectedUser(user.id)}
                            style={{
                                padding: '15px',
                                cursor: 'pointer',
                                backgroundColor: isSelected ? '#e9ecef' : 'white',
                                borderBottom: '1px solid #f1f1f1',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                transition: 'background-color 0.2s',
                                color: '#333' // Force dark text
                            }}
                            onMouseEnter={(e) => { if(!isSelected) e.currentTarget.style.backgroundColor = '#f8f9fa'; }}
                            onMouseLeave={(e) => { if(!isSelected) e.currentTarget.style.backgroundColor = 'white'; }}
                        >
                            <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                <div style={{ fontWeight: '600', color: '#212529' }}>{user.firstName} {user.lastName}</div>
                                <div style={{ fontSize: '0.8em', color: '#6c757d' }}>{user.email}</div>
                            </div>
                            {hasMessages && <span style={{ color: '#007bff', fontSize: '20px', lineHeight: 0 }}>•</span>}
                        </div>
                    );
                })}

                {users.length === 0 && (
                    <div style={{ padding: '15px', color: '#6c757d', fontSize: '0.9em', textAlign: 'center' }}>
                        No users found.
                    </div>
                )}
            </div>

            {/* Chat Area */}
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', backgroundColor: '#fff' }}>
                {selectedUser ? (
                    <>
                        <div style={{
                            padding: '15px',
                            backgroundColor: '#fff',
                            borderBottom: '1px solid #dee2e6',
                            color: '#333',
                            fontWeight: 'bold',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '10px'
                        }}>
                            <div style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: '#007bff', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>
                                {users.find(u => u.id === selectedUser)?.firstName?.charAt(0) || '#'}
                            </div>
                            <span style={{ color: '#333' }}>
                                {users.find(u => u.id === selectedUser)
                                    ? `${users.find(u => u.id === selectedUser).firstName} ${users.find(u => u.id === selectedUser).lastName}`
                                    : `User #${selectedUser}`}
                            </span>
                        </div>

                        <div style={{
                            flex: 1,
                            padding: '20px',
                            overflowY: 'auto',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '10px',
                            backgroundColor: '#f5f7fb'
                        }}>
                            {(activeChats[selectedUser] || []).length === 0 && (
                                <div style={{ textAlign: 'center', color: '#adb5bd', marginTop: '20px' }}>
                                    Start a conversation...
                                </div>
                            )}
                            {(activeChats[selectedUser] || []).map((msg, index) => (
                                <div key={index} style={{
                                    alignSelf: msg.sender === 'ADMIN' ? 'flex-end' : 'flex-start',
                                    backgroundColor: msg.sender === 'ADMIN' ? '#007bff' : 'white',
                                    color: msg.sender === 'ADMIN' ? 'white' : '#333',
                                    padding: '12px 16px',
                                    borderRadius: msg.sender === 'ADMIN' ? '15px 15px 0 15px' : '15px 15px 15px 0',
                                    maxWidth: '70%',
                                    wordWrap: 'break-word',
                                    boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                                    fontSize: '14px'
                                }}>
                                    {msg.content}
                                </div>
                            ))}
                            <div ref={messagesEndRef} />
                        </div>

                        <form onSubmit={sendMessage} style={{
                            padding: '20px',
                            borderTop: '1px solid #dee2e6',
                            display: 'flex',
                            gap: '10px',
                            backgroundColor: 'white'
                        }}>
                            <input
                                type="text"
                                value={input}
                                onChange={(e) => setInput(e.target.value)}
                                placeholder="Type a message..."
                                style={{
                                    flex: 1,
                                    padding: '12px 20px',
                                    borderRadius: '25px',
                                    border: '1px solid #e0e0e0',
                                    outline: 'none',
                                    backgroundColor: '#f8f9fa',
                                    color: '#333'
                                }}
                            />
                            <button
                                type="submit"
                                disabled={!input.trim()}
                                style={{
                                    backgroundColor: '#007bff',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '25px',
                                    padding: '0 25px',
                                    cursor: 'pointer',
                                    fontWeight: '600',
                                    opacity: !input.trim() ? 0.7 : 1
                                }}
                            >
                                Send
                            </button>
                        </form>
                    </>
                ) : (
                    <div style={{
                        flex: 1,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#6c757d',
                        backgroundColor: '#f5f7fb',
                        flexDirection: 'column',
                        gap: '10px'
                    }}>
                        <div style={{ fontSize: '40px' }}>💬</div>
                        <div>Select a user to start chatting</div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminChat;
