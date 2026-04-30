const normalize = (role) => {
  if (!role) return ''
  return String(role).replace(/^ROLE_/i, '').trim().toLowerCase()
}

export function getRole(user) {
  if (!user) return ''
  if (typeof user === 'string') {
    return normalize(user)
  }
  if (Array.isArray(user.roles) && user.roles.length > 0) {
    return normalize(user.roles[0])
  }
  return normalize(user.role)
}

export function hasRole(user, expectedRole) {
  if (!expectedRole) return true
  return getRole(user) === normalize(expectedRole)
}

export default {
  getRole,
  hasRole,
}