-- Step 1: Add a temporary column to hold the table numbers
ALTER TABLE restaurant_tables ADD COLUMN temp_number INTEGER;

-- Step 2: Copy the table numbers to the temporary column
UPDATE restaurant_tables SET temp_number = number;

-- Step 3: Drop the original number column (this drops the global unique constraint/index on it)
ALTER TABLE restaurant_tables DROP COLUMN number;

-- Step 4: Re-add the number column without the unique constraint
ALTER TABLE restaurant_tables ADD COLUMN number INTEGER;

-- Step 5: Copy the table numbers back from the temporary column
UPDATE restaurant_tables SET number = temp_number;

-- Step 6: Set the number column to NOT NULL
ALTER TABLE restaurant_tables MODIFY COLUMN number INTEGER NOT NULL;

-- Step 7: Drop the temporary column
ALTER TABLE restaurant_tables DROP COLUMN temp_number;

-- Step 8: Add the composite unique constraint on (restaurant_id, number)
ALTER TABLE restaurant_tables ADD CONSTRAINT uq_restaurant_tables_tenant_number UNIQUE (restaurant_id, number);
