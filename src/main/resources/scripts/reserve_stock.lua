-- Check if the key exists first
local stock_exists = redis.call('EXISTS', KEYS[1])
if (stock_exists == 0) then
    return -1  -- Return -1 to indicate "Product Not Found"
end

local current_stock = tonumber(redis.call('GET', KEYS[1]))
if (current_stock < tonumber(ARGV[1])) then
    return 0   -- Return 0 for "Out of Stock"
else
    redis.call('DECRBY', KEYS[1], ARGV[1])
    return 1   -- Return 1 for "Success"
end