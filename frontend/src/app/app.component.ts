import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <button class="theme-toggle" (click)="toggleTheme()" [attr.aria-label]="isDarkMode ? 'Activer le mode clair' : 'Activer le mode sombre'">
      <span *ngIf="!isDarkMode">🌙</span>
      <span *ngIf="isDarkMode">☀️</span>
    </button>
    <router-outlet></router-outlet>
  `,
  styles: [`
    .theme-toggle {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 1000;
      width: 50px;
      height: 50px;
      border-radius: 50%;
      border: 2px solid var(--border-color);
      background: var(--bg-primary);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      box-shadow: var(--shadow-md);
      transition: all 0.3s ease;
    }

    .theme-toggle:hover {
      transform: scale(1.1);
      box-shadow: var(--shadow-lg);
    }

    .theme-toggle:active {
      transform: scale(0.95);
    }
  `]
})
export class AppComponent implements OnInit {
  title = 'FESUP - Gestion des Vœux';
  isDarkMode = false;

  ngOnInit() {
    // Récupérer la préférence sauvegardée
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.isDarkMode = true;
      document.body.classList.add('dark-mode');
    } else {
      // Détecter la préférence système
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      if (prefersDark) {
        this.isDarkMode = true;
        document.body.classList.add('dark-mode');
      }
    }
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.body.classList.add('dark-mode');
      localStorage.setItem('theme', 'dark');
    } else {
      document.body.classList.remove('dark-mode');
      localStorage.setItem('theme', 'light');
    }
  }
}
