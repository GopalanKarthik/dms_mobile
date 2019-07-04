const _ = require('lodash');
const odoo = require('../odoo_server');
const base = require('./base');

class Lead {
    async getDashboardCounts(user) {
        let result = [];
        try {
            let server = odoo.getOdoo(user.email);
            let model = 'crm.lead';
            let states = ["overdue", "today", "planned"];
            let self = this;
            for (let i = 0; i < states.length; i++) {
                console.log("state is :", states[i]);
                let group = await server.read_group(model, { domain: this.getActivityDomain(states[i]), groupby: ["stage_id"] }, true);
                console.log(model + '', group);
                result.push({ state: states[i], result: group });
            };
            return result;
        } catch (err) {
            return { error: err.message || err.toString() };
        }

    }
    async getEnquiry(user, { id }) {
        let result = null;
        try {
            let server = odoo.getOdoo(user.email);
            let model = 'dms.enquiry';
            console.log("Hello from get enquiry");
            let enq_id = await server.search(model, { domain: [["opportunity_ids", "in", [id]]] });
            if (!enq_id) throw new Error('Enquiry Id for this opportunity is not present');
            result = await base.getModel(user, { model: model, id: enq_id });
            console.log(model + '', result);
        } catch (err) {
            return { error: err.message || err.toString() };
        }
        return result;
    }
    getActivityDomain(state) {
        let domain = [];
        var today = new Date().toISOString().slice(0, 10);
        switch (state) {
            case "planned":
                domain.push(["activity_date_deadline", ">", today]);
                break;
            case "today":
                domain.push(["activity_date_deadline", "=", today]);
                break;
            case "overdue":
                domain.push(["activity_date_deadline", "<", today]);
                break;
        }
        return domain;
    };
    async searchLeadsByState(user, { state, stage }) {
        let result = null;
        try {
            let server = odoo.getOdoo(user.email);
            let model = 'crm.lead';
            console.log("State:", state, stage);
            let domain = this.getActivityDomain(state);
            domain.push(["stage_id.name", "ilike", stage]);
            result = await server.search_read(model, { domain: domain, fields: ["name", "id", "date_deadline", "mobile", "partner_name", "user_id", "team_id", "stage_id"] });
            console.log(model + '', result);
        } catch (err) {
            return { error: err.message || err.toString() };
        }
        return result;
    }
    async createActivity(user, req) {
        let result = null;
        try {
            let server = odoo.getOdoo(user.email);
            let ir_model = 'ir.model';
            let model = 'mail.activity';
            let domain = [];
            domain.push(["model", "=", "crm.lead"]);
            let ir_models = await server.search_read(ir_model, { domain: domain, fields: ["name","model", "id"] });
            console.log(ir_model + '', ir_models);
            if(ir_models.records.length > 0){
                req['res_model_id'] = ir_models.records[0].id;
                console.log("Added model id to create json",req);
            } else {
                throw new Error("Cannot create Activity");
            }
            let result = await server.create(model, req);
            console.log(model, result);
            if(result == null){
                throw new Error("Cannot create Activity");
            }
            return { "id": result, "Message": "Success" };
        } catch (err) {
            return { error: err.message || err.toString() };
        }
    }
    async getActivities(user, { id }) {
        let result = null;
        try {
            let server = odoo.getOdoo(user.email);
            let model = 'mail.activity';
            console.log("Id is - ", id);
            let domain = [];
            domain.push(["res_model", "=", "crm.lead"]);
            domain.push(["res_id", "=", id]);
            result = await server.search_read(model, { domain: domain, fields: ["name", "id", "date_deadline", "summary", "note", "activity_type_id","user_id","res_model"] });
            console.log(model + '', result);
        } catch (err) {
            return { error: err.message || err.toString() };
        }
        return base.cleanModels(result.records);
    }
    async setActivities(user, { id, feedback }) {
        let result = null;
        try {
            let server = odoo.getOdoo(user.email);
            let model = 'mail.activity';
            console.log("Feedback is  - ", feedback);
            result = await server.action_feedback(model, { id: id, feedback: feedback });
            console.log(model + '', result);
        } catch (err) {
            return { error: err.message || err.toString() };
        }
        return result;
    }
    async getLeadDashboard(user,{ id }) {
        let result = [];
        try {
            let server = odoo.getOdoo(user.email);
            let model = 'crm.lead';
            let domain = [];
            let domain1 = [];
            let fields = ["user_id","user_id_count","user_booked_id"];
            domain.push(["team_id", "=", parseInt(id)]);
            //domain1.push(["stage_id.name", "ilike","booked"]);
            domain1.push(["team_id", "=", parseInt(id)]);
            domain1.push(["stage_id", "=", 4]);
            let self = this;
                let group = await server.read_group(model, {domain: domain, groupby: ["user_id"], fields: fields }, true);
                console.log("The model and groups are ",model + '', group[0].user_id[0]);
               
                let group1 = await server.read_group(model, {domain: domain1, groupby: ["user_id"] }, true);

                 for(var i=0 ; i<group.length ; i++){
                    for(var j=0; j<group1.length ; j++){
                        console.log("inside for group is ",group[i].user_id[0] == group1[j].user_id[0]);
                        if(group[i].user_id[0] == group1[j].user_id[0]){
                            group[i].user_booked_id = group1[j].user_id_count;
                        }
                    }
                } 
                result.push({ result: group });
            return result;
        } catch (err) {
            return { error: err.message || err.toString() };
        }

    }
}

module.exports = new Lead();
