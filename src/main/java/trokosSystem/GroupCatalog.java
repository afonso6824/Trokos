package trokosSystem;

import exception.GroupAlreadyExistException;

import javax.crypto.SealedObject;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GroupCatalog implements Serializable {
    private static final String GROUPS_BACKUP_PATH = "src/main/resources/backup/groups.cif";
    private static GroupCatalog instance;
    private HashMap<String, Group> groupsList;

    private GroupCatalog() {
        initializeGroupsWithBackup();
    }

    public static GroupCatalog getInstance() {
        if (instance == null) {
            instance = new GroupCatalog();
        }
        return instance;
    }

    public HashMap<String, Group> getGroupsList() {
        return groupsList;
    }

    public void addGroup(Group group) throws GroupAlreadyExistException {
        if (GroupCatalog.getInstance().existsGroup(group.getID())) {
            throw new GroupAlreadyExistException();
        }
        this.groupsList.put(group.getID(), group);
        Backup.getInstance().updateBackup();
    }

    public boolean existsGroup(String groupID) {
        return this.groupsList.containsKey(groupID);
    }

    public Group findGroupByGroupID(String groupID) {
        return this.groupsList.get(groupID);
    }

    public List<Group> getGroupsWhoOwnerIs(Client client) {
        return groupsList.values()
                .stream()
                .filter(group -> group.getOwner().equals(client))
                .collect(Collectors.toList());
    }

    public List<Group> getGroupsWhereBelongs(Client client) {
        return groupsList.values()
                .stream()
                .filter(group -> group.existsClientInGroup(client.getUsername()))
                .collect(Collectors.toList());
    }


    private void initializeGroupsWithBackup() {
        try (FileInputStream fileIn = new FileInputStream(GROUPS_BACKUP_PATH);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

            this.groupsList = Backup.getInstance().decryptGroups((SealedObject) objectIn.readObject());

        } catch (IOException | ClassNotFoundException e) {
            groupsList = new HashMap<>();
        }
    }
}
