# Local-only runner image for `act` (make ci-local).
# Docker >= 28 refuses to copy into /var/run when it is a symlink ("path escapes from
# parent"), which breaks act's job setup on the catthehacker images. Make it a real dir.
FROM catthehacker/ubuntu:act-latest
RUN if [ -L /var/run ]; then rm /var/run && mkdir -p /var/run; fi \
 && mkdir -p /var/run/act
