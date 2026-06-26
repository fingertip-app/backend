# API Migration Guide - Payment Endpoint Change

## Breaking Change: Payment Endpoint Parameter Changed

### Changed Endpoint
```
POST /api/v1/reservations/{reservationId}/payment
```

### What Changed

**Before (Old API):**
```http
POST /api/v1/reservations/{reservationId}/payment?paymentKey=tok_xxxxx
```
- **Parameter**: `paymentKey` (required) - External payment gateway token from Toss Payments
- **Purpose**: Verify payment was processed by external PG

**After (New API):**
```http
POST /api/v1/reservations/{reservationId}/payment?paymentMethod=CARD
```
- **Parameter**: `paymentMethod` (optional, default: "CARD") - Payment method type
- **Purpose**: Mock payment for demo/testing (no real PG integration)

### Migration Steps

#### For Mobile App (React Native)
```typescript
// OLD CODE (REMOVE THIS)
const response = await apiPost(`/reservations/${reservationId}/payment`, {
  paymentKey: tossPaymentKey  // From Toss Payments SDK
});

// NEW CODE
const response = await apiPost(`/reservations/${reservationId}/payment`, {
  paymentMethod: "CARD"  // or "TRANSFER", "MOBILE", etc.
});
```

#### API Request Examples

**Old Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/reservations/123/payment?paymentKey=tok_abc123xyz" \
  -H "Authorization: Bearer {token}"
```

**New Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/reservations/123/payment?paymentMethod=CARD" \
  -H "Authorization: Bearer {token}"
```

### Why This Changed

- **Reason**: Competition demo version - no real payment gateway integration
- **Impact**: System now uses mock payments instead of actual PG processing
- **When**: Implemented in backend commit [COMMIT_HASH]

### Backend Changes

The backend now:
1. Accepts `paymentMethod` string instead of `paymentKey`
2. Generates mock payment records internally via `PaymentService.createMockPayment()`
3. No longer validates against external payment gateway

### Rollback Plan

If you need to revert to real payment processing:
1. Restore `ReservationService.processPayment(Long, Long, String paymentKey)` signature
2. Remove `PaymentService` mock payment creation
3. Re-integrate Toss Payments validation
4. Update API parameter back to `paymentKey` (required)

### Testing

**Test the new endpoint:**
```bash
# Approve reservation first
curl -X POST "http://localhost:8080/api/v1/reservations/123/approve"

# Process mock payment
curl -X POST "http://localhost:8080/api/v1/reservations/123/payment?paymentMethod=CARD" \
  -H "Authorization: Bearer {token}"

# Verify status changed to PAID
curl -X GET "http://localhost:8080/api/v1/reservations/123" \
  -H "Authorization: Bearer {token}"
```

### Questions?

Contact: qazwsx12098@naver.com
