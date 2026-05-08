// frontend-web/src/components/common/AlertBell.jsx
import React, { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Bell, AlertTriangle } from 'lucide-react';
import alertService from '../../services/alertService';

const AlertBell = () => {
    const [notifications, setNotifications] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [unreadCount, setUnreadCount] = useState(0);
    const wrapperRef = useRef(null);
    const panelRef = useRef(null);
    const [panelStyle, setPanelStyle] = useState({ top: 64, right: 16 });

    useEffect(() => {
        fetchNotifications();
        // Poll every 10 seconds for new alerts
        const interval = setInterval(fetchNotifications, 10000);
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        const onClickOutside = (event) => {
            const clickedWrapper = wrapperRef.current?.contains(event.target);
            const clickedPanel = panelRef.current?.contains(event.target);
            if (!clickedWrapper && !clickedPanel) {
                setShowDropdown(false);
            }
        };
        const onEscape = (event) => {
            if (event.key === 'Escape') {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', onClickOutside);
        document.addEventListener('keydown', onEscape);
        return () => {
            document.removeEventListener('mousedown', onClickOutside);
            document.removeEventListener('keydown', onEscape);
        };
    }, []);

    useEffect(() => {
        if (!showDropdown) return;
        const positionPanel = () => {
            const rect = wrapperRef.current?.getBoundingClientRect();
            if (!rect) return;
            const panelWidth = Math.min(352, window.innerWidth - 16);
            const right = Math.max(8, window.innerWidth - rect.right);
            const top = rect.bottom + 10;
            const maxRight = Math.max(8, window.innerWidth - panelWidth - 8);
            setPanelStyle({
                top,
                right: Math.min(right, maxRight),
            });
        };

        positionPanel();
        window.addEventListener('resize', positionPanel);
        window.addEventListener('scroll', positionPanel, true);
        return () => {
            window.removeEventListener('resize', positionPanel);
            window.removeEventListener('scroll', positionPanel, true);
        };
    }, [showDropdown]);

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
            default: return 'border-[#667D9D] bg-[#667D9D]/10 text-[#667D9D]';
        }
    };

    const getTypeIcon = (type) => {
        switch (type) {
            case 'alert': return <AlertTriangle size={14} />;
            case 'warning': return <AlertTriangle size={14} />;
            default: return <Bell size={14} />;
        }
    };

    const handleMarkRead = async (id) => {
        try {
            await alertService.markNotificationRead(id);
            setNotifications(prev => prev.map(n => (n.id === id ? { ...n, is_read: true } : n)));
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
        }
    };

    const handleMarkAllRead = async () => {
        try {
            await alertService.markAllNotificationsRead();
            setNotifications(prev => prev.map(n => ({ ...n, is_read: true })));
            setUnreadCount(0);
        } catch (error) {
            console.error('Failed to mark all notifications as read:', error);
        }
    };

    return (
        <div className="relative z-[1200]" ref={wrapperRef}>
            <button
                onClick={() => setShowDropdown(!showDropdown)}
                aria-expanded={showDropdown}
                aria-label="Notifications"
                className={`relative p-2 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#667D9D]/60 ${
                    showDropdown
                        ? 'bg-[#667D9D]/15 text-[#667D9D] ring-1 ring-[#667D9D]/40'
                        : 'text-slate-400 hover:text-slate-700 dark:hover:text-white hover:bg-slate-200/70 dark:hover:bg-slate-800/70'
                }`}
            >
                <Bell size={18} />
                {unreadCount > 0 && (
                    <span className="absolute top-0 right-0 w-4 h-4 bg-red-500 rounded-full text-xs text-white flex items-center justify-center">
                        {unreadCount}
                    </span>
                )}
            </button>

            {showDropdown && createPortal(
                <>
                    <div
                        className="fixed inset-0 z-[2147483645] bg-transparent"
                        onClick={() => setShowDropdown(false)}
                    />
                    <div
                        ref={panelRef}
                        style={{ top: panelStyle.top, right: panelStyle.right }}
                        className="fixed w-[22rem] max-w-[calc(100vw-1rem)] glass rounded-xl border shadow-2xl z-[2147483646]"
                    >
                    <div className="p-3 border-b border-slate-200/80 dark:border-slate-700 flex items-center justify-between gap-2">
                        <h3 className="text-sm font-semibold text-slate-800 dark:text-white">Notifications</h3>
                        {unreadCount > 0 && (
                            <button
                                onClick={handleMarkAllRead}
                                className="text-xs font-medium text-[#667D9D] hover:text-[#667D9D] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#667D9D]/60 rounded"
                            >
                                Mark all read
                            </button>
                        )}
                    </div>
                    <div className="max-h-[70vh] overflow-y-auto">
                        {notifications.length === 0 ? (
                            <div className="p-4 text-center text-slate-500 text-sm">
                                No notifications
                            </div>
                        ) : (
                            notifications.map(notif => (
                                <div
                                    key={notif.id}
                                    className={`p-3 border-b border-slate-200/80 dark:border-slate-700 hover:bg-slate-200/60 dark:hover:bg-slate-700/50 transition-colors ${!notif.is_read ? 'bg-[#667D9D]/8' : ''}`}
                                >
                                    <div className="flex items-start gap-2">
                                        <div className={`mt-0.5 ${getSeverityColor(notif.type)}`}>
                                            {getTypeIcon(notif.type)}
                                        </div>
                                        <div className="flex-1">
                                            <p className="text-sm font-medium text-slate-800 dark:text-white">
                                                {notif.title}
                                            </p>
                                            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                                                {notif.message}
                                            </p>
                                            <p className="text-xs text-slate-500 mt-1">
                                                {new Date(notif.created_at).toLocaleTimeString()}
                                            </p>
                                        </div>
                                        {!notif.is_read && (
                                            <button
                                                onClick={() => handleMarkRead(notif.id)}
                                                className="text-xs text-[#667D9D] hover:text-[#667D9D] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#667D9D]/60 rounded"
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
                </>
            , document.body)}
        </div>
    );
};

export default AlertBell;