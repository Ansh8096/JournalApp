package net.engineerAnsh.journalApp.Services;

import net.bytebuddy.asm.MemberSubstitution;
import net.engineerAnsh.journalApp.Entity.User;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

// UserArgumentProvider is a custom implementation of the JUnit 5 interface ArgumentsProvider ,
// It’s used with @ArgumentsSource(UserArgumentProvider.class) in a parameterized test — to supply complex test data (like objects) to the test method....
public class UserArgumentProvider implements ArgumentsProvider {

    // This method will return a Stream of Arguments , Each element in the stream corresponds to one run of the test method....
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(
                Arguments.of(User.builder().userName("Priya").password("Priya").build()), // his creates a stream of User objects using the builder pattern, Each 'Arguments.of()' wraps a User object...
                Arguments.of(User.builder().userName("Shreya").password("Shreya").build()),
                Arguments.of(User.builder().userName("Anamika").password("").build())
        );
    }
}
