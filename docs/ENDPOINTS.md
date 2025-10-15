# API Endpoints

## Autenticación

### POST `auth/login`
Solicita iniciar sesión con correo y contraseña.

```bash
curl -X POST "$AUTH_BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "usuario@correo.com", "password": "secreto"}'
```

Respuesta:
```json
{
  "authToken": "token",
  "user": {
    "id": "1",
    "name": "Usuario",
    "email": "usuario@correo.com"
  }
}
```

### GET `auth/me`
Obtiene la información del usuario autenticado usando el token.

```bash
curl -X GET "$AUTH_BASE_URL/me" -H "Authorization: Bearer $TOKEN"
```

## Productos

### GET `product`
Lista los productos disponibles.

```bash
curl -X GET "$STORE_BASE_URL/product" -H "Authorization: Bearer $TOKEN"
```

### POST `product`
Crea un producto nuevo.

```bash
curl -X POST "$STORE_BASE_URL/product" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Producto",
    "description": "Descripción",
    "price": 20.5,
    "images": [
      { "url": "https://..." }
    ]
  }'
```

## Cargas de Imágenes

### POST `upload`
Sube una imagen en formato multipart.

```bash
curl -X POST "$STORE_BASE_URL/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "image=@/ruta/a/imagen.jpg"
```

Respuesta esperada:
```json
{
  "id": "123",
  "url": "https://cdn...",
  "mime": "image/jpeg"
}
```
