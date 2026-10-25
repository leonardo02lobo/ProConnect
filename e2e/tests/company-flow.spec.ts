import { ApiClient } from '../fixtures/api';
import { waitForMail } from '../fixtures/mailpit';
import { expect, test } from '../fixtures/test';
import { ADMIN, PASSWORD } from '../fixtures/users';

/**
 * add-role-management: full company flow with role promotion. The ADMIN promotes a plain user to
 * COMPANY_ADMIN → that user creates a company and a job → another user applies → the owner accepts →
 * Mailpit receives the acceptance mail for the applicant.
 */
test('promoted COMPANY_ADMIN runs the company → job → postulation → acceptance mail flow', async ({ uid }) => {
  const admin = await ApiClient.create();
  const owner = await ApiClient.create();
  const applicant = await ApiClient.create();
  try {
    await admin.login(ADMIN.email, ADMIN.password);
    const ownerUser = await owner.registerAndLogin(uid, 'owner', PASSWORD);
    const applicantUser = await applicant.registerAndLogin(uid, 'applicant', PASSWORD);

    // Promotion by the ADMIN; the owner keeps the session opened before it.
    const promoted = await admin.setRole(ownerUser.id, 'COMPANY_ADMIN');
    expect(promoted.status()).toBe(200);
    expect((await promoted.json()).role).toBe('COMPANY_ADMIN');

    const companyName = `Globex ${uid}`;
    const companyRes = await owner.ctx.post('/api/companies', {
      data: { name: companyName, description: 'E2E company', location: 'Remote' },
    });
    expect(companyRes.status()).toBe(201);
    const company = await companyRes.json();
    expect(company.userId).toBe(ownerUser.id);

    const jobTitle = `Platform Engineer ${uid}`;
    const jobRes = await owner.ctx.post('/api/jobs', {
      data: { companyId: company.id, title: jobTitle, description: 'E2E job', workStation: 'Platform' },
    });
    expect(jobRes.status()).toBe(201);
    const job = await jobRes.json();

    // A plain USER can neither create companies nor resolve postulations.
    expect(
      (await applicant.ctx.post('/api/companies', { data: { name: `Nope ${uid}`, description: 'x', location: 'y' } }))
        .status(),
    ).toBe(403);

    const postulationRes = await applicant.ctx.post('/api/postulations', { data: { jobId: job.id } });
    expect(postulationRes.status()).toBe(201);
    const postulation = await postulationRes.json();
    expect(postulation.status).toBe('PENDING');
    expect((await applicant.ctx.post(`/api/postulations/${postulation.id}/accept`)).status()).toBe(403);

    const accepted = await owner.ctx.post(`/api/postulations/${postulation.id}/accept`);
    expect(accepted.status()).toBe(200);
    expect((await accepted.json()).status).toBe('ACCEPTED');

    const mails = await waitForMail(applicantUser.email);
    expect(mails).toHaveLength(1);
    expect(mails[0].To.map((t) => t.Address)).toEqual([applicantUser.email]);
    expect(mails[0].Subject).toBe(`Respuesta hacia el puesto de: ${jobTitle}`);
    expect(mails[0].Text).toContain(`Hola ${applicantUser.name}`);
    expect(mails[0].Text).toContain(companyName);
    expect(mails[0].Text).toContain('Felicidades');
  } finally {
    await admin.dispose();
    await owner.dispose();
    await applicant.dispose();
  }
});
