import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { UserContextService } from '../../services/user-context-service';

import { Home } from './home';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('should create', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[aria-label="Loading products"]')).toBeTruthy();
    httpTesting.expectOne('https://localhost:8080/api/v1/products').flush([]);
    fixture.detectChanges();
    expect(component).toBeTruthy();
    expect(component.isLoading()).toBe(false);
    expect(component.products()).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('No products yet');
  });

  it('shows an error when products fail to load', () => {
    fixture.detectChanges();
    httpTesting.expectOne('https://localhost:8080/api/v1/products')
      .flush('Unavailable', { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();

    expect(component.hasError()).toBe(true);
    expect(component.isLoading()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('Products could not be loaded');
  });

  it('renders seller products and delegates logout', () => {
    localStorage.setItem('auth_token', 'Bearer token');
    TestBed.inject(UserContextService).load().subscribe();
    httpTesting.expectOne('https://localhost:8080/api/v1/users/me').flush({
      id: 'seller-1',
      name: 'Seller',
      email: 'seller@example.com',
      avatar: null,
      role: 'SELLER',
    });

    fixture.detectChanges();
    httpTesting.expectOne('https://localhost:8080/api/v1/products').flush([
      {
        id: 'product-1',
        name: 'Chair',
        description: 'Wood chair',
        price: 20,
        quantity: 0,
        userId: 'seller-1',
        imageUrls: ['chair.png'],
      },
      {
        id: 'product-2',
        name: 'Table',
        description: 'Oak table',
        price: 75,
        quantity: 2,
        userId: 'seller-1',
        imageUrls: [],
      },
    ]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Hi, Seller');
    expect(fixture.nativeElement.textContent).toContain('Dashboard');
    expect(fixture.nativeElement.querySelector('img')?.getAttribute('src')).toBe('chair.png');
    expect(fixture.nativeElement.textContent).toContain('Out of stock');
    expect(fixture.nativeElement.textContent).toContain('No image');
    expect(component.imageFor(component.products()[0])).toBe('chair.png');

    component.logout();
    expect(localStorage.getItem('auth_token')).toBeNull();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Sign in');
  });
});
