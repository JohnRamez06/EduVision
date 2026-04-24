import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Brain, Eye, Zap, BarChart3, Users, Shield, ArrowRight, GraduationCap } from 'lucide-react'

const PARTICLES = Array.from({ length: 22 }, (_, i) => ({
  id: i,
  top:  `${Math.random() * 90 + 5}%`,
  left: `${Math.random() * 90 + 5}%`,
  size: Math.random() * 3 + 1.5,
  delay: Math.random() * 6,
  duration: Math.random() * 4 + 5,
  opacity: Math.random() * 0.5 + 0.15,
}))

const FEATURES = [
  { icon: Eye,       label: 'Real-Time Vision',    desc: 'Computer vision monitors engagement live during lectures' },
  { icon: BarChart3, label: 'Emotion Analytics',   desc: 'Deep emotional intelligence across every student' },
  { icon: Zap,       label: 'Instant Alerts',      desc: 'Lecturers notified the moment attention drops' },
  { icon: Shield,    label: 'Privacy First',        desc: 'Consent-driven, anonymized, fully secure' },
]

export default function SplashPage() {
  const navigate = useNavigate()
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 80)
    return () => clearTimeout(t)
  }, [])

  return (
    <div className="relative min-h-screen bg-navy-900 overflow-hidden bg-grid flex flex-col">

      {/* Ambient orbs */}
      <div className="orb w-[600px] h-[600px] bg-blue-600/20 -top-40 -left-40 animate-float-slow" />
      <div className="orb w-[500px] h-[500px] bg-violet-600/15 -bottom-32 -right-32 animate-float" />
      <div className="orb w-[300px] h-[300px] bg-indigo-500/10 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 animate-float-fast" />

      {/* Floating particles */}
      {PARTICLES.map(p => (
        <span
          key={p.id}
          className="absolute rounded-full bg-blue-400 pointer-events-none"
          style={{
            top: p.top, left: p.left,
            width: p.size, height: p.size,
            opacity: p.opacity,
            animation: `float ${p.duration}s ${p.delay}s ease-in-out infinite`,
          }}
        />
      ))}

      {/* Navbar */}
      <nav className={`relative z-10 flex items-center justify-between px-8 py-5 transition-all duration-700 ${visible ? 'opacity-100' : 'opacity-0'}`}>
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center animate-glow-pulse">
            <GraduationCap size={18} className="text-white" />
          </div>
          <span className="text-lg font-bold tracking-tight text-white">EduVision</span>
        </div>
        <button
          onClick={() => navigate('/login')}
          className="text-sm text-slate-400 hover:text-white transition-colors duration-200 font-medium"
        >
          Sign in
        </button>
      </nav>

      {/* Hero */}
      <main className="relative z-10 flex-1 flex flex-col items-center justify-center text-center px-6 py-12">

        {/* Icon */}
        <div className={`mb-8 transition-all duration-700 delay-100 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
          <div className="relative inline-flex items-center justify-center w-28 h-28">
            {/* Orbit ring */}
            <div className="absolute inset-0 rounded-full border border-blue-500/20 animate-spin-slow" />
            <div className="absolute inset-3 rounded-full border border-violet-500/15 animate-spin-slow" style={{ animationDirection: 'reverse', animationDuration: '18s' }} />
            {/* Core */}
            <div className="relative w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-500 via-indigo-500 to-violet-600 flex items-center justify-center shadow-2xl animate-glow-pulse">
              <Brain size={36} className="text-white" />
            </div>
            {/* Orbiting dot */}
            <span className="absolute w-3 h-3 rounded-full bg-blue-400 shadow-lg shadow-blue-400/50 animate-orbit" />
          </div>
        </div>

        {/* Badge */}
        <div className={`mb-5 transition-all duration-700 delay-150 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
          <span className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold text-blue-300 bg-blue-500/10 border border-blue-500/20">
            <Zap size={11} className="fill-blue-400 text-blue-400" />
            AI-Powered Education Analytics
          </span>
        </div>

        {/* Headline */}
        <h1 className={`text-5xl sm:text-6xl lg:text-7xl font-black tracking-tight leading-none mb-5 transition-all duration-700 delay-200 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
          <span className="text-white">See</span>{' '}
          <span className="gradient-text">Beyond</span>
          <br />
          <span className="text-white">the Classroom</span>
        </h1>

        {/* Sub */}
        <p className={`max-w-xl text-slate-400 text-lg leading-relaxed mb-10 transition-all duration-700 delay-300 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
          EduVision uses real-time emotion recognition to help lecturers understand
          student engagement — and intervene before anyone falls behind.
        </p>

        {/* CTAs */}
        <div className={`flex flex-col sm:flex-row items-center gap-4 mb-20 transition-all duration-700 delay-[400ms] ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
          <button
            onClick={() => navigate('/login')}
            className="btn-primary flex items-center gap-2 text-base"
          >
            Get Started
            <ArrowRight size={16} />
          </button>
          <button
            onClick={() => navigate('/register')}
            className="px-8 py-3 rounded-xl font-semibold text-slate-300 border border-slate-700 hover:border-slate-500 hover:text-white transition-all duration-200 text-base"
          >
            Create Account
          </button>
        </div>

        {/* Feature cards */}
        <div className={`grid grid-cols-2 lg:grid-cols-4 gap-4 max-w-4xl w-full transition-all duration-700 delay-500 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
          {FEATURES.map(({ icon: Icon, label, desc }) => (
            <div key={label} className="glass rounded-2xl p-5 text-left hover:border-blue-500/30 transition-all duration-300 hover:-translate-y-1 group">
              <div className="w-9 h-9 rounded-lg bg-blue-500/15 flex items-center justify-center mb-3 group-hover:bg-blue-500/25 transition-colors">
                <Icon size={18} className="text-blue-400" />
              </div>
              <p className="text-sm font-semibold text-slate-200 mb-1">{label}</p>
              <p className="text-xs text-slate-500 leading-snug">{desc}</p>
            </div>
          ))}
        </div>
      </main>

      {/* Footer */}
      <footer className={`relative z-10 text-center py-6 text-xs text-slate-600 transition-all duration-700 delay-[600ms] ${visible ? 'opacity-100' : 'opacity-0'}`}>
        © 2026 EduVision · Built for smarter education
      </footer>
    </div>
  )
}
