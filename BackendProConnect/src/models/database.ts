import { createPool, Pool } from 'mysql2/promise';

const pool: Pool = createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || 'root1234',
  database: process.env.DB_NAME || 'ProConnect',
});

console.log('Conexión a MySQL establecida');
export default pool;