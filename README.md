# HustStay Backend

Hệ thống backend của HustStay gồm kiến trúc microservices với Spring Boot, sử dụng Eureka làm Service Discovery và API Gateway làm điểm vào duy nhất.

---

## Kiến trúc tổng quan

```
Client (FE)
    │
    ▼
API Gateway :1111          ← Điểm vào duy nhất, xác thực JWT
    │
    ├── user-service    :8081   (Quản lý người dùng, xác thực)
    ├── room-service    :8082   (Quản lý phòng)
    ├── hotel-service   :8083   (Quản lý khách sạn)
    ├── booking-service :8084   (Đặt phòng)
    ├── review-service  :8085   (Đánh giá, bình luận)
    └── payment-service :8087   (Thanh toán PayOS)

Eureka Server       :8761   ← Service Registry (tất cả service đăng ký vào đây)
```

---

## Yêu cầu hệ thống

| Công cụ | Phiên bản tối thiểu |
|---------|---------------------|
| Docker Desktop | 24+ |
| Docker Compose | v2+ |
| Java JDK (nếu chạy thủ công) | 17 |
| Maven (nếu chạy thủ công) | 3.9+ |

---

## Cách 1: Chạy bằng Docker Compose (Khuyến nghị)

### Bước 1 — Build và khởi động toàn bộ services

```bash
cd BE
docker compose up --build
```

> Lần đầu sẽ mất 5–10 phút để Maven build tất cả các service.

### Bước 2 — Chạy nền (detached mode)

```bash
docker compose up --build -d
```

### Bước 3 — Kiểm tra các service đang chạy

```bash
docker compose ps
```

### Dừng toàn bộ

```bash
docker compose down
```

### Dừng và xóa volumes

```bash
docker compose down -v
```

---

## Cách 2: Chạy từng service thủ công (Maven)

> Cần chạy theo **đúng thứ tự** vì các service phụ thuộc vào Eureka Server.

### Bước 1 — Khởi động Eureka Server (bắt buộc chạy trước)

```bash
cd BE/eureka-server
mvn spring-boot:run
```

Truy cập Eureka Dashboard: http://localhost:8761

### Bước 2 — Khởi động API Gateway

```bash
cd BE/api-gateway
mvn spring-boot:run
```

### Bước 3 — Khởi động các microservices (theo bất kỳ thứ tự nào)

```bash
# Mở terminal riêng cho từng service
cd BE/user-service    && mvn spring-boot:run
cd BE/room-service    && mvn spring-boot:run
cd BE/hotel-service   && mvn spring-boot:run
cd BE/booking-service && mvn spring-boot:run
cd BE/review-service  && mvn spring-boot:run
cd BE/payment-service && mvn spring-boot:run
```

---

## Cổng mặc định

| Service | Cổng | Mô tả |
|---------|------|-------|
| Eureka Server | `8761` | Service Registry, Dashboard tại http://localhost:8761 |
| API Gateway | `1111` | Cổng vào chính cho toàn bộ API |
| User Service | `8081` | Đăng ký, đăng nhập, quản lý tài khoản |
| Room Service | `8082` | Quản lý phòng khách sạn |
| Hotel Service | `8083` | Quản lý thông tin khách sạn |
| Booking Service | `8084` | Đặt phòng, lịch sử đặt phòng |
| Review Service | `8085` | Bình luận, đánh giá sao |
| Payment Service | `8087` | Thanh toán qua PayOS |

---

## Routing qua API Gateway

Tất cả request từ Frontend đều đi qua `http://localhost:1111`. API Gateway sẽ forward đến service tương ứng:

| Prefix URL | Forward đến |
|------------|-------------|
| `/user-service/**` | `user-service:8081` |
| `/room-service/**` | `room-service:8082` |
| `/hotel-service/**` | `hotel-service:8083` |
| `/booking-service/**` | `booking-service:8084` |
| `/review-service/**` | `review-service:8085` |
| `/payment-service/**` | `payment-service:8087` |

**Ví dụ:**
```
GET http://localhost:1111/user-service/api/users/me
→ forward → GET http://user-service:8081/api/users/me
```

---

## Cơ sở dữ liệu

Dự án hỗ trợ 2 chế độ kết nối database:

| Chế độ | Địa chỉ | Mô tả |
|--------|---------|-------|
| **GCP Cloud SQL** (mặc định) | `34.21.247.183:5432` | Kết nối thẳng, không cần setup |
| **Local PostgreSQL** | `localhost:5432` | Cần restore từ file backup |

| Database | Service sử dụng |
|----------|----------------|
| `user_service` | user-service |
| `room_service` | room-service |
| `hotel_service` | hotel-service |
| `booking_service` | booking-service |
| `review_service` | review-service |
| `payment_service` | payment-service |

---

## Chạy Database Local (từ backup DBeaver)

> Dùng khi muốn phát triển offline hoặc không muốn phụ thuộc GCP Cloud SQL.

### Yêu cầu

- PostgreSQL 15+ đã được cài và đang chạy trên máy
- `psql` có trong PATH (thường đi kèm khi cài PostgreSQL)
- Thư mục backup: `db_backup/` (chứa 6 file `.sql`)

---

### Bước 1 — Restore từng database từ file backup

Mở terminal và chạy lần lượt từng lệnh dưới đây (nhập password PostgreSQL khi được hỏi):

```bash
# Restore user_service
psql -U postgres -f db_backup/user_service.sql

# Restore room_service
psql -U postgres -f db_backup/room_service.sql

# Restore hotel_service
psql -U postgres -f db_backup/hotel_service.sql

# Restore booking_service
psql -U postgres -f db_backup/booking_service.sql

# Restore review_service
psql -U postgres -f db_backup/review_service.sql

# Restore payment_service
psql -U postgres -f db_backup/payment_service.sql
```

> **Lưu ý:** File backup đã chứa lệnh `CREATE DATABASE` nên không cần tạo database trước.

---

### Bước 2 — Kiểm tra database đã được tạo

```bash
psql -U postgres -c "\l"
```

Kết quả mong đợi — thấy 6 database:

```
user_service
room_service
hotel_service
booking_service
review_service
payment_service
```

---

### Bước 3 — Cập nhật docker-compose.yml để trỏ về localhost

Mở file `docker-compose.yml`, tìm và thay tất cả `34.21.247.183` thành `host.docker.internal` (địa chỉ Docker trỏ về máy host):

```yaml
# Trước (GCP):
- DB_URL=jdbc:postgresql://34.21.247.183:5432/user_service

# Sau (Local):
- DB_URL=jdbc:postgresql://host.docker.internal:5432/user_service
```

> Trên **Linux**, thay `host.docker.internal` bằng `172.17.0.1` hoặc IP thật của máy.

Thay tất cả 6 service trong `docker-compose.yml`:

| Service | DB_URL local |
|---------|-------------|
| user-service | `jdbc:postgresql://host.docker.internal:5432/user_service` |
| room-service | `jdbc:postgresql://host.docker.internal:5432/room_service` |
| hotel-service | `jdbc:postgresql://host.docker.internal:5432/hotel_service` |
| booking-service | `jdbc:postgresql://host.docker.internal:5432/booking_service` |
| review-service | `jdbc:postgresql://host.docker.internal:5432/review_service` |
| payment-service | `jdbc:postgresql://host.docker.internal:5432/payment_service` |

---

### Bước 4 — Khởi động backend với DB local

```bash
cd BE
docker compose up --build -d
```

---

### Chạy thủ công (Maven) với DB local

Nếu chạy bằng `mvn spring-boot:run`, cần thêm biến môi trường để override DB URL, ví dụ:

```bash
cd BE/user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/user_service --spring.datasource.username=postgres --spring.datasource.password=<your_password>"
```

Hoặc tạo file `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/user_service
spring.datasource.username=postgres
spring.datasource.password=<your_password>
```

Rồi chạy với profile local:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---


## Build riêng từng service (không chạy)

Nếu chỉ muốn build JAR mà không chạy:

```bash
cd BE/<tên-service>
mvn clean package -DskipTests
```

File JAR được tạo tại: `<tên-service>/target/*.jar`

---

## Xem logs từng service (Docker)

```bash
# Xem log realtime của một service cụ thể
docker compose logs -f api-gateway
docker compose logs -f eureka-server
docker compose logs -f user-service

# Xem log của tất cả services
docker compose logs -f
```

---

## Khởi động lại một service cụ thể

```bash
docker compose restart api-gateway
docker compose restart user-service
```

---

## Rebuild một service sau khi sửa code

```bash
docker compose up --build api-gateway -d
```
