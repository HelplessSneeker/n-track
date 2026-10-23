import { Component } from '@angular/core';

import { TodayPage } from './today/today-page';

@Component({
  imports: [TodayPage],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {}
