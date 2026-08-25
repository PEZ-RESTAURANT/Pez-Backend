-- Migration V24: Update customers schema to support non-affiliated, document numbers and last payment method.
ALTER TABLE customers MODIFY phone VARCHAR(50) NULL;
ALTER TABLE customers ADD COLUMN document_number VARCHAR(50) NULL;
ALTER TABLE customers ADD COLUMN last_payment_method VARCHAR(50) NULL;
