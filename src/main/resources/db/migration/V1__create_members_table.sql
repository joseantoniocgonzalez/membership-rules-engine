CREATE TABLE members (
    member_number VARCHAR(20) PRIMARY KEY,
    access_code VARCHAR(20) NOT NULL,
    membership_type VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL,
    member_since DATE NOT NULL
);
