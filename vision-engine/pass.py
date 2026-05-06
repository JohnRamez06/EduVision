import bcrypt

password = "password123"
hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
print("Password:", password)
print("Hash:", hashed.decode('utf-8'))