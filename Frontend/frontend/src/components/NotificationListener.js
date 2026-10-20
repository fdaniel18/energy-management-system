import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useAuth } from '../context/AuthContext';

const NotificationListener = () => {
    const { userId } = useAuth();
    const [notification, setNotification] = useState(null);

    useEffect(() => {
        if (!userId) return;

        // Connect to the WebsocketMicroservice
        const socket = new SockJS('http://localhost:8084/ws');

        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: (str) => {
                // console.log(str);
            },
            onConnect: () => {
                console.log('Connected to WebSocket');
                stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
                    if (message.body) {
                        const body = JSON.parse(message.body);
                        setNotification(body.message);
                        // Auto-dismiss after 5 seconds
                        setTimeout(() => setNotification(null), 5000);
                    }
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, [userId]);

    if (!notification) return null;

    return (
        <div style={{
            position: 'fixed',
            top: '80px',
            right: '20px',
            backgroundColor: '#ff4444',
            color: 'white',
            padding: '15px',
            borderRadius: '8px',
            zIndex: 9999,
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            maxWidth: '300px',
            animation: 'slideIn 0.3s ease-out'
        }}>
            <div style={{ fontWeight: 'bold', marginBottom: '5px' }}>⚠️ Overconsumption Alert</div>
            <div>{notification}</div>
        </div>
    );
};

export default NotificationListener;
