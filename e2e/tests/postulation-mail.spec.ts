import { ApiClient } from '../fixtures/api';
import { waitForMail } from '../fixtures/mailpit';
import { expect, test } from '../fixtures/test';
import { ADMIN, PASSWORD } from '../fixtures/users';

/**
 * Spec job-application-notifications: the company owner accepts a postulation and the applicant receives the
 * positive mail at the email stored in the database (Mailpit captures it). API-only flow: there is no
 * postulations UI yet.
 */
test('owner accepts a postulation and the applicant gets the mail', async ({ uid }) => {
  const admin = await ApiClient.create();
  const applicant = await ApiClient.create();
  try {
    await admin.login(ADMIN.email, ADMIN.password);
    const company = await (await admin.ctx.post('/api/companies', {
      data: { name: `Acme ${uid}`, description: 'E2E company', location: 'Remote' },
    })).json();
    const jobTitle = `Backend Dev ${uid}`;
    const jobRes = await admin.ctx.post('/api/jobs', {
      data: { companyId: company.id, title: jobTitle, description: 'E2E job', workStation: 'Backend' },
    });
    expect(jobRes.status()).toBe(201);
    const job = await jobRes.json();

    const email = `applicant-${uid}@proconnect.test`;
    const name = `Ana ${uid}`;
    expect(await applicant.register({ name, userName: `ana_${uid}`.replace(/-/g, '_'), email, password: PASSWORD })).toBe(201);
    await applicant.login(email, PASSWORD);
    const created = await applicant.ctx.post('/api/postulations', { data: { jobId: job.id } });
    expect(created.status()).toBe(201);
    const postulation = await created.json();
    expect(postulation.status).toBe('PENDING');

    // The applicant is not the company owner: 403 and no mail.
    expect((await applicant.ctx.post(`/api/postulations/${postulation.id}/accept`)).status()).toBe(403);

    // A recipient in the body is ignored: the mail goes to the stored email only.
    const accepted = await admin.ctx.post(`/api/postulations/${postulation.id}/accept`, {
      data: { correoDestino: `attacker-${uid}@evil.test` },
    });
    expect(accepted.status()).toBe(200);
    expect((await accepted.json()).status).toBe('ACCEPTED');

    const mails = await waitForMail(email);
    expect(mails).toHaveLength(1);
    expect(mails[0].To.map((t) => t.Address)).toEqual([email]);
    expect(mails[0].Subject).toBe(`Respuesta hacia el puesto de: ${jobTitle}`);
    expect(mails[0].Text).toContain(`Hola ${name}`);
    expect(mails[0].Text).toContain(jobTitle);
    expect(mails[0].Text).toContain(`Acme ${uid}`);
    expect(mails[0].Text).toContain('Felicidades');
    expect(await waitForMail(`attacker-${uid}@evil.test`, 1_500)).toHaveLength(0);

    // Already resolved: 409.
    expect((await admin.ctx.post(`/api/postulations/${postulation.id}/reject`)).status()).toBe(409);
  } finally {
    await admin.dispose();
    await applicant.dispose();
  }
});
