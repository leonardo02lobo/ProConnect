import { request } from '@playwright/test';
import { MAILPIT_URL } from './env';

export interface MailpitMessage {
  ID: string;
  Subject: string;
  To: Array<{ Address: string; Name: string }>;
  Text: string;
}

/**
 * Polls Mailpit until exactly the messages addressed to `to` are available (or the timeout expires) and
 * returns them with their plain-text body.
 */
export async function waitForMail(to: string, timeoutMs = 20_000): Promise<MailpitMessage[]> {
  const ctx = await request.newContext({ baseURL: MAILPIT_URL });
  const deadline = Date.now() + timeoutMs;
  try {
    while (Date.now() < deadline) {
      const res = await ctx.get('/api/v1/search', { params: { query: `to:"${to}"` } });
      if (res.ok()) {
        const { messages } = (await res.json()) as { messages: Array<{ ID: string }> };
        if (messages.length > 0) {
          return Promise.all(
            messages.map(async (m) => (await (await ctx.get(`/api/v1/message/${m.ID}`)).json()) as MailpitMessage),
          );
        }
      }
      await new Promise((r) => setTimeout(r, 500));
    }
    return [];
  } finally {
    await ctx.dispose();
  }
}
