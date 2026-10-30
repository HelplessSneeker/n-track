import { HttpClient, HttpErrorResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

// Für die TODOs unten schon hereingeholt — noch benutzt sie keiner.
import { Plan } from './plan';
import { firstValueFrom } from 'rxjs';

/**
 * Der Datenzugriff auf den Tagesplan: eine Quelle der Wahrheit für die ganze Ansicht.
 *
 * `@Service()` statt `@Injectable({ providedIn: 'root' })` — so schreibt man Singletons ab v22.
 */
@Service()
export class TodayStore {
  private readonly http = inject(HttpClient);

  readonly plan = httpResource<Plan>(() => '/api/today');

  readonly warnings = computed(() => this.plan.value()?.warnings || []);
  readonly current = computed(() => this.plan.value()?.items.find((i) => i.status === 'CURRENT'));
  readonly upcoming = computed(
    () => this.plan.value()?.items.filter((i) => i.status === 'UPCOMING') || [],
  );
  readonly done = computed(() => this.plan.value()?.items.filter((i) => i.status === 'DONE') || []);
  readonly lastDone = computed(() => this.done()?.at(-1));

  readonly busy = signal(false);
  readonly error = signal<string | undefined>(undefined);

  async markDone(id: number): Promise<void> {
    this.busy.set(true);
    this.error.set(undefined);

    try {
      this.plan.set(await firstValueFrom(this.http.post<Plan>(`/api/items/${id}/done`, {})));
    } catch (e) {
      this.errorHandler(e);
    } finally {
      this.busy.set(false);
    }
  }

  async undoDone(id: number): Promise<void> {
    this.busy.set(true);
    this.error.set(undefined);

    try {
      this.plan.set(await firstValueFrom(this.http.delete<Plan>(`/api/items/${id}/done`)));
    } catch (e) {
      this.errorHandler(e);
    } finally {
      this.busy.set(false);
    }
  }

  private errorHandler(e: any): void {
    const unexpectedErrorMessage = 'Etwas ist schiefgelaufen, versuchen sie es später erneut.';
    if (e instanceof HttpErrorResponse) {
      this.error.set(e.error.detail || unexpectedErrorMessage);
    } else {
      this.error.set(unexpectedErrorMessage);
    }
    this.plan.reload();
  }
}
