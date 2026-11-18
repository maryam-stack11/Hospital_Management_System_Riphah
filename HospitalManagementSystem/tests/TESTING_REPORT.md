# Testing Report

**Environment:** OpenJDK 21.0.13 (javac 21.0.13), Linux. All results below come from runs actually executed during development.

## 1. Compilation
`javac -Xlint:all -d out src/*.java` initially gave 2 warnings (missing `serialVersionUID` in two exception classes). They were fixed. Final compile of `src/*.java` and `tests/HospitalTests.java`: **0 errors, 0 warnings**.

## 2. Automated business-rule harness (`tests/HospitalTests.java`)
Run with `./tests/run_tests.sh`. **80 checks, 80 passed, 0 failed.** It runs on temporary folders, never on `data/`. Covered: patient validation (age, phone, name), auto IDs, search, update; doctor validation, specialization search; appointment booking, unknown patient/doctor, doctor and patient double booking, past date, cancellation, slot reuse after cancellation, illegal status changes; availability (unavailable doctor, daily capacity, recovery after cancel); billing (fee from doctor, BigDecimal total, duplicate bill, negative charges, partial/paid/unpaid, revenue and outstanding); delete rules; status counts; CSV persistence round trip (including commas in text); corrupted, empty and missing files; date/time/money parsing; polymorphism.

## 3. Scripted console sessions (`tests/scripts/`)
The real program was run with input redirected from script files; outputs are saved next to the scripts.

| # | Script | What was verified | Result |
|---|---|---|---|
| 1 | `01_full_workflow.txt` | Invalid menu input (`9`, `abc`, blank); patient registration with invalid age/gender/phone/address retries; view, search by ID and name (found and not found), update, aborted delete; doctor add (custom specialization), view, search, update, availability view and schedule, delete; appointment booking, bad date/time format, doctor clash, patient clash, past date, unknown patient, unavailable doctor, search, by patient/doctor, cancel, slot reuse, complete, illegal status change; bill generation (invalid amount retry, total Rs. 4000.00), duplicate bill, bill for scheduled appointment, search, history, partial then full payment; statistics; exit | Passed - every expected `[SUCCESS]`/`[ERROR]` message appeared |
| 2 | `02_restart_persistence.txt` | **Restart:** program reloaded 2 patients, 6 doctors, 3 appointments, 1 bill; delete patient with an active appointment (cancelled then deleted); delete patient with completed history refused; statistics | Passed |
| 4 | `04_second_restart.txt` | Second restart showed deletions persisted (1 patient, 1 appointment, 1 bill) and statistics matched | Passed |
| 3 | `03_input_closed.txt` | Input stream ends abruptly: program saves and exits cleanly | Passed |

## 4. Mapping to the required test list
Compile (1-2) done; launch (3) done; patient registration/search (4-5), doctor registration (6), booking (7), duplicate prevention (8), cancellation (9), billing (10), payment updates (11), statistics (12), invalid menu input (13), exit (14) and persistence across restart (15) were all exercised in scripts 1-2 and the harness.

## 5. Bugs and issues found, and fixed
1. Two compiler warnings (`serialVersionUID`) - fixed.
2. Generate Bill asked for additional charges *before* checking that the appointment was billable, so a duplicate/ineligible bill wasted the user's input - now checked first (`checkBillable`).
3. Deleting a patient/doctor with completed history asked for confirmation and only then refused - now refused immediately.
4. Statistics labels were misaligned (`Total bills generated:`) - column widened.
5. Harness expectation error (wrong next doctor ID after a deletion) and several mistakes in my own first versions of the input scripts were corrected; these were test bugs, not application bugs.

## 6. Not tested / limitations of testing
- The application was **not** run on Windows, so `run.bat` and `tests\run_tests.bat` are untested; nor was it opened in the VS Code GUI (only JDK 21 command line was used).
- Interactive typing in a real terminal was not exercised; input was piped from files.
- Only JDK 21 was used (source uses `switch ->` arrows, so JDK 14+ is required).
- Scripts use the date 2030-01-15 for "future" appointments; this stays valid until that date.
