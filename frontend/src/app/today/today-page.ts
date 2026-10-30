import { Component, inject } from '@angular/core';

import { CurrentItem } from './current-item/current-item';
import { DoneList } from './done-list/done-list';
import { UpcomingList } from './upcoming-list/upcoming-list';
import { WarningList } from './warning-list/warning-list';
import { TodayStore } from './today-store';

/**
 * Die Jetzt-Ansicht. Der einzige Ort, der den Store kennt — die vier Kindkomponenten bekommen
 * alles über Inputs und melden sich über Outputs zurück.
 */
@Component({
  selector: 'app-today-page',
  imports: [WarningList, CurrentItem, UpcomingList, DoneList],
  templateUrl: './today-page.html',
  styleUrl: './today-page.scss',
})
export class TodayPage {
  protected readonly today = inject(TodayStore);
}
