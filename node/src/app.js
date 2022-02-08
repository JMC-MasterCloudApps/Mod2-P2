import express from 'express';
const app = express();

import FilmController from './controllers/FilmController.js';
app.use('/api/films', FilmController);
export default app;
