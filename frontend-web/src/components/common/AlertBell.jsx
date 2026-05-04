// frontend-web/src/components/common/AlertBell.jsx
import React, { useState, useEffect } from 'react';
import { Bell, AlertTriangle, CheckCircle, XCircle } from 'lucide-react';
import alertService from '../../services/alertService';

const AlertBell = () => {
    const [alerts, setAlerts] = useState([]);
    const [notifications, setNotifications] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        fetchNotifications();
        // Poll every 10 seconds for new alerts
        const interval = setInterval(fetchNotifications, 10000);
        return () => clearInterval(interval);
    }, []);

    const fetchNotifications = async () => {
        try {
            const data = await alertService.getNotifications();
            setNotifications(data);
            setUnreadCount(data.filter(n => !n.is_read).length);
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        }
    };

    const getSeverityColor = (severity) => {
        switch (severity) {
            case 'critical': return 'border-red-500 bg-red-500/10 text-red-400';
            case 'warning': return 'border-yellow-500 bg-yellow-500/10 text-yellow-400';
            default: return 'border-blue-500 bg-blue-500/10 text-blue-400';
        }
    };

    const getTypeIcon = (type) => {
        switch (type) {
            case 'alert': return <AlertTriangle size={14} />;
            case 'warning': return <AlertTriangle size={14} />;
            default: return <Bell size={14} />;
        }
    };

    return (
        <div className="relative">
            <button
                onClick={() => setShowDropdown(!showDropdown)}
                className="relative p-2 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors"
            >
                <Bell size={18} className="text-slate-400" />
                {unreadCount > 0 && (
                    <span className="absolute top-0 right-0 w-4 h-4 bg-red-500 rounded-full text-xs text-white flex items-center justify-center">
                        {unreadCount}
                    </span>
                )}
            </button>

            {showDropdown && (
                <div className="absolute right-0 mt-2 w-80 bg-slate-800 rounded-xl border border-slate-700 shadow-xl z-50">
                    <div className="p-3 border-b border-slate-700">
                        <h3 className="text-sm font-semibold text-white">Notifications</h3>
                    </div>
                    <div className="max-h-96 overflow-y-auto">
                        {notifications.length === 0 ? (
                            <div className="p-4 text-center text-slate-500 text-sm">
                                No notifications
                            </div>
                        ) : (
                            notifications.map(notif => (
                                <div
                                    key={notif.id}
                                    className={`p-3 border-b border-slate-700 hover:bg-slate-700/50 transition-colors ${!notif.is_read ? 'bg-slate-700/30' : ''}`}
                                >
                                    <div className="flex items-start gap-2">
                                        <div className={`mt-0.5 ${getSeverityColor(notif.type)}`}>
                                            {getTypeIcon(notif.type)}
                                        </div>
                                        <div className="flex-1">
                                            <p className="text-sm font-medium text-white">
                                                {notif.title}
                                            </p>
                                            <p className="text-xs text-slate-400 mt-0.5">
                                                {notif.message}
                                            </p>
                                            <p className="text-xs text-slate-500 mt-1">
                                                {new Date(notif.created_at).toLocaleTimeString()}
                                            </p>
                                        </div>
                                        {!notif.is_read && (
                                            <button
                                                onClick={() => alertService.markNotificationRead(notif.id)}
                                                className="text-xs text-blue-400 hover:text-blue-300"
                                            >
                                                Mark read
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default AlertBell;