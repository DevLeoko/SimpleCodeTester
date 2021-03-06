<template>
  <crud-modify-actions
    ref="modify-actions"
    @requestFinished="requestPending = false"
    :repository="users"
    :element="user"
  >
    <span slot="preActions" v-if="!user.enabled" class="subheading disabled mr-4">(Disabled)</span>

    <template slot="customMenuActions">
      <v-list-tile @click="blackhole">
        <v-list-tile-title v-if="!user.enabled" @click="setEnabled(user, true)">Enable user</v-list-tile-title>
        <v-list-tile-title v-else @click="setEnabled(user,false)">Disable user</v-list-tile-title>
      </v-list-tile>

      <v-list-tile @click="blackhole">
        <v-dialog v-model="changeDialogOpened" max-width="600">
          <v-list-tile-title slot="activator">Change password</v-list-tile-title>
          <change-password @input="submitPasswordChange" :canSubmit="!requestPending"></change-password>
        </v-dialog>
      </v-list-tile>

      <v-list-tile @click="blackhole">
        <v-dialog v-model="editDialogOpened" max-width="600">
          <v-list-tile-title slot="activator">Edit user</v-list-tile-title>
          <edit-user
            @close="editDialogOpened = false"
            :users="users"
            :user="userCopy"
            @user="submitUserChange"
            :canSubmit="!requestPending"
            editing="true"
          ></edit-user>
        </v-dialog>
      </v-list-tile>
    </template>
  </crud-modify-actions>
</template>

<script lang="ts">
import Vue from "vue";
import { Prop, Component } from "vue-property-decorator";
import { Users, User, UserToAdd } from "@/components/users/Users";
import { extractErrorMessage } from "@/util/requests";
import ChangePassword from "@/components/users/ChangePassword.vue";
import Axios from "axios";
import CrudModifyActions from "@/components/crud/CrudModifyActions.vue";
import UserModificationComponent from "@/components/users/UserModificationComponent.vue";

@Component({
  components: {
    "change-password": ChangePassword,
    "crud-modify-actions": CrudModifyActions,
    "edit-user": UserModificationComponent
  }
})
export default class UserModifyActions extends Vue {
  private changeDialogOpened = false;
  private editDialogOpened = false;
  private requestPending = false;

  @Prop()
  private user!: User;

  @Prop()
  private users!: Users;

  get userCopy() {
    return new User(
      this.user.id,
      this.user.displayName,
      this.user.enabled,
      this.user.roles
    );
  }

  setEnabled(user: User, enabled: boolean) {
    this.handlePromise(this.users.setEnabled(user, enabled));
  }

  handlePromise(promise: Promise<any>) {
    promise
      .catch(error => this.$emit("error", extractErrorMessage(error)))
      .finally(() => (this.requestPending = false));
  }

  submitPasswordChange(password: string) {
    this.requestPending = true;
    this.changeDialogOpened = false;

    const userToAdd = new UserToAdd(
      this.user.id,
      this.user.displayName,
      this.user.roles,
      password
    );
    this.$emit("updated", userToAdd);

    (this.$refs["modify-actions"] as CrudModifyActions<
      User,
      UserToAdd
    >).updateElement(userToAdd);
  }

  submitUserChange(user: UserToAdd) {
    this.requestPending = true;
    this.editDialogOpened = false;

    (this.$refs["modify-actions"] as CrudModifyActions<
      User,
      UserToAdd
    >).updateElement(user);
  }

  blackhole() {}
}
</script>

<style scoped>
.disabled {
  color: #ff6347;
}
</style>

