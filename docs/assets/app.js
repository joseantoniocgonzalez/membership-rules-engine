const members = {
    "BHFC-1001": { accessCode: "111111", type: "SEASON_TICKET_HOLDER", active: true, memberSince: "2015-06-01" },
    "BHFC-12000": { accessCode: "120000", type: "SEASON_TICKET_HOLDER", active: true, memberSince: "2014-05-10" },
    "BHFC-18050": { accessCode: "180500", type: "SEASON_TICKET_HOLDER", active: true, memberSince: "2016-09-03" },
    "BHFC-27000": { accessCode: "270000", type: "SEASON_TICKET_HOLDER", active: true, memberSince: "2017-03-22" },
    "BHFC-36035": { accessCode: "360350", type: "SEASON_TICKET_HOLDER", active: true, memberSince: "2018-08-01" },
    "BHFC-1900": { accessCode: "190000", type: "SEASON_TICKET_HOLDER", active: false, memberSince: "2013-04-12" },
    "BHFC-2045": { accessCode: "482910", type: "PREMIUM_MEMBER", active: true, memberSince: "2021-07-14" },
    "BHFC-3107": { accessCode: "739204", type: "STANDARD_MEMBER", active: true, memberSince: "2023-02-10" },
    "BHFC-4880": { accessCode: "105377", type: "FREE_MEMBER", active: true, memberSince: "2024-09-03" }
};

const ticketPurchases = new Set();
const finalAllocation = 3;
const finalApplications = new Map();

function validateMember(memberNumber, accessCode) {
    const member = members[memberNumber];

    if (!member || member.accessCode !== accessCode) {
        return { valid: false };
    }

    return { valid: true, member };
}

function statusClass(status) {
    if (["CONFIRMED", "ALREADY_INCLUDED", "CANCELLED"].includes(status)) {
        return "success";
    }

    if (["WAITING_LIST", "NOT_YET_AVAILABLE", "SOLD_OUT", "DUPLICATE_REQUEST"].includes(status)) {
        return "warning";
    }

    return "danger";
}

function renderResult(targetId, status, lines) {
    const target = document.getElementById(targetId);

    target.innerHTML = `
        <span class="status-pill ${statusClass(status)}">${status}</span>
        ${lines.map(line => `<p class="result-line">${line}</p>`).join("")}
    `;
}

function calculateDiscount() {
    const memberNumber = document.getElementById("store-member").value.trim();
    const accessCode = document.getElementById("store-code").value.trim();
    const basePrice = Number(document.getElementById("store-price").value);
    const validation = validateMember(memberNumber, accessCode);

    if (!validation.valid) {
        renderResult("store-result", "INVALID_MEMBER_ACCESS", [
            "The member number or access code is invalid.",
            `Final price: £${basePrice.toFixed(2)}`
        ]);
        return;
    }

    const discounts = {
        SEASON_TICKET_HOLDER: 20,
        PREMIUM_MEMBER: 20,
        STANDARD_MEMBER: 10,
        FREE_MEMBER: 0
    };

    const discount = discounts[validation.member.type];
    const finalPrice = basePrice * ((100 - discount) / 100);

    renderResult("store-result", "CONFIRMED", [
        `<strong>Membership type:</strong> ${validation.member.type}`,
        `<strong>Discount:</strong> ${discount}%`,
        `<strong>Final price:</strong> £${finalPrice.toFixed(2)}`
    ]);
}

function requestTicket() {
    const memberNumber = document.getElementById("ticket-member").value.trim();
    const accessCode = document.getElementById("ticket-code").value.trim();
    const requestDate = document.getElementById("ticket-date").value;
    const validation = validateMember(memberNumber, accessCode);

    if (!validation.valid) {
        renderResult("ticket-result", "INVALID_MEMBER_ACCESS", [
            "The member number or access code is invalid."
        ]);
        return;
    }

    const member = validation.member;

    if (!member.active) {
        renderResult("ticket-result", "REJECTED", [
            "Inactive members cannot purchase match tickets."
        ]);
        return;
    }

    if (member.type === "SEASON_TICKET_HOLDER") {
        renderResult("ticket-result", "ALREADY_INCLUDED", [
            "Season Ticket Holders already have access included for this home match."
        ]);
        return;
    }

    if (ticketPurchases.has(memberNumber)) {
        renderResult("ticket-result", "DUPLICATE_REQUEST", [
            "This member has already purchased a ticket for this match."
        ]);
        return;
    }

    const windows = {
        PREMIUM_MEMBER: "2026-07-01",
        STANDARD_MEMBER: "2026-07-08",
        FREE_MEMBER: "2026-07-15"
    };

    if (requestDate < windows[member.type]) {
        renderResult("ticket-result", "NOT_YET_AVAILABLE", [
            `The sale window for ${member.type} is not open yet.`
        ]);
        return;
    }

    ticketPurchases.add(memberNumber);

    renderResult("ticket-result", "CONFIRMED", [
        "Ticket purchase confirmed.",
        `Membership type: ${member.type}`
    ]);
}

function rebalanceFinalApplications() {
    const activeApplications = [...finalApplications.values()]
        .filter(application => application.status !== "CANCELLED")
        .sort((a, b) => a.memberSince.localeCompare(b.memberSince) || a.memberNumber.localeCompare(b.memberNumber));

    activeApplications.forEach((application, index) => {
        application.status = index < finalAllocation ? "CONFIRMED" : "WAITING_LIST";
        finalApplications.set(application.memberNumber, application);
    });

    renderAllocationPanel();
}

function requestFinalTicket() {
    const memberNumber = document.getElementById("final-member").value.trim();
    const accessCode = document.getElementById("final-code").value.trim();
    const validation = validateMember(memberNumber, accessCode);

    if (!validation.valid) {
        renderResult("final-result", "INVALID_MEMBER_ACCESS", [
            "The member number or access code is invalid."
        ]);
        return;
    }

    const member = validation.member;

    if (!member.active) {
        renderResult("final-result", "REJECTED", [
            "Inactive members cannot request a Promotion Final ticket."
        ]);
        return;
    }

    if (member.type !== "SEASON_TICKET_HOLDER") {
        renderResult("final-result", "NOT_ELIGIBLE", [
            "Only active Season Ticket Holders are eligible for the Promotion Final allocation."
        ]);
        return;
    }

    if (finalApplications.has(memberNumber)) {
        renderResult("final-result", "DUPLICATE_REQUEST", [
            "This member has already requested a Promotion Final ticket."
        ]);
        return;
    }

    finalApplications.set(memberNumber, {
        memberNumber,
        memberSince: member.memberSince,
        status: "WAITING_LIST"
    });

    rebalanceFinalApplications();

    const status = finalApplications.get(memberNumber).status;

    renderResult("final-result", status, [
        status === "CONFIRMED"
            ? "Promotion Final ticket confirmed."
            : "The allocation is full. The member has been placed on the waiting list.",
        `Member since: ${member.memberSince}`
    ]);
}

function resetFinalDemo() {
    finalApplications.clear();

    [
        ["BHFC-1001", members["BHFC-1001"]],
        ["BHFC-18050", members["BHFC-18050"]],
        ["BHFC-27000", members["BHFC-27000"]],
        ["BHFC-36035", members["BHFC-36035"]]
    ].forEach(([memberNumber, member]) => {
        finalApplications.set(memberNumber, {
            memberNumber,
            memberSince: member.memberSince,
            status: "WAITING_LIST"
        });
    });

    rebalanceFinalApplications();

    renderResult("final-result", "CONFIRMED", [
        "Demo allocation has been reset.",
        "The reduced sample shows confirmed and waiting list members."
    ]);
}

function renderAllocationPanel() {
    const confirmed = [...finalApplications.values()]
        .filter(application => application.status === "CONFIRMED")
        .sort((a, b) => a.memberSince.localeCompare(b.memberSince));

    const waiting = [...finalApplications.values()]
        .filter(application => application.status === "WAITING_LIST")
        .sort((a, b) => a.memberSince.localeCompare(b.memberSince));

    document.getElementById("confirmed-list").innerHTML = confirmed.map(application =>
        `<li><code>${application.memberNumber}</code><span>${application.memberSince}</span></li>`
    ).join("");

    document.getElementById("waiting-list").innerHTML = waiting.map(application =>
        `<li><code>${application.memberNumber}</code><span>${application.memberSince}</span></li>`
    ).join("");
}

function loadDemo(type) {
    const demos = {
        "store-premium": () => {
            document.getElementById("store-member").value = "BHFC-2045";
            document.getElementById("store-code").value = "482910";
            document.getElementById("store-price").value = "75";
            calculateDiscount();
        },
        "store-standard": () => {
            document.getElementById("store-member").value = "BHFC-3107";
            document.getElementById("store-code").value = "739204";
            document.getElementById("store-price").value = "75";
            calculateDiscount();
        },
        "store-invalid": () => {
            document.getElementById("store-member").value = "BHFC-2045";
            document.getElementById("store-code").value = "000000";
            calculateDiscount();
        },
        "ticket-season": () => {
            document.getElementById("ticket-member").value = "BHFC-1001";
            document.getElementById("ticket-code").value = "111111";
            document.getElementById("ticket-date").value = "2026-07-02";
            requestTicket();
        },
        "ticket-standard-early": () => {
            document.getElementById("ticket-member").value = "BHFC-3107";
            document.getElementById("ticket-code").value = "739204";
            document.getElementById("ticket-date").value = "2026-07-02";
            requestTicket();
        },
        "ticket-sold-out": () => {
            renderResult("ticket-result", "SOLD_OUT", [
                "No tickets are available for this match."
            ]);
        },
        "final-confirmed": () => {
            resetFinalDemo();
            document.getElementById("final-member").value = "BHFC-12000";
            document.getElementById("final-code").value = "120000";
            requestFinalTicket();
        },
        "final-waiting": () => {
            resetFinalDemo();
            document.getElementById("final-member").value = "BHFC-36035";
            document.getElementById("final-code").value = "360350";
            renderResult("final-result", "WAITING_LIST", [
                "BHFC-36035 is eligible but outside the reduced demo allocation.",
                "In the backend, this status is calculated by seniority."
            ]);
        },
        "final-not-eligible": () => {
            document.getElementById("final-member").value = "BHFC-2045";
            document.getElementById("final-code").value = "482910";
            requestFinalTicket();
        },
        "final-inactive": () => {
            document.getElementById("final-member").value = "BHFC-1900";
            document.getElementById("final-code").value = "190000";
            requestFinalTicket();
        }
    };

    demos[type]?.();
}

document.getElementById("calculate-discount").addEventListener("click", calculateDiscount);
document.getElementById("request-ticket").addEventListener("click", requestTicket);
document.getElementById("run-final-rule").addEventListener("click", requestFinalTicket);
document.getElementById("reset-final-demo").addEventListener("click", resetFinalDemo);

document.querySelectorAll("[data-demo]").forEach(button => {
    button.addEventListener("click", () => loadDemo(button.dataset.demo));
});

resetFinalDemo();
