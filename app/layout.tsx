import './globals.css';
import type { ReactNode } from 'react';
export const metadata = { title: 'FAANG Engineering Tracker', description: '6-month Google/Amazon/Meta preparation tracker' };
export default function RootLayout({children}:{children:ReactNode}) {
  return <html lang="en"><body>{children}</body></html>;
}